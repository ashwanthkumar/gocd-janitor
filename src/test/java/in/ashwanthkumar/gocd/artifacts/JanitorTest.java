package in.ashwanthkumar.gocd.artifacts;

import in.ashwanthkumar.gocd.actions.Action;
import in.ashwanthkumar.gocd.artifacts.config.JanitorConfiguration;
import in.ashwanthkumar.gocd.artifacts.config.PipelineConfig;
import in.ashwanthkumar.gocd.client.GoCD;
import in.ashwanthkumar.gocd.client.types.PipelineDependency;
import in.ashwanthkumar.gocd.client.types.PipelineRunStatus;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.collections.Maps;
import in.ashwanthkumar.utils.collections.Sets;
import in.ashwanthkumar.utils.lang.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static in.ashwanthkumar.gocd.client.types.PipelineRunStatus.FAILED;
import static in.ashwanthkumar.gocd.client.types.PipelineRunStatus.PASSED;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JanitorTest {
    Action action = mock(Action.class);
    GoCD client;
    JanitorConfiguration config = new JanitorConfiguration();

    @Before
    public void before() {
        client = mock(GoCD.class);
        config = new JanitorConfiguration();
    }

    private Janitor janitor() {
        return new Janitor(action, config, client);
    }

    @Test
    public void shouldReturnPipelinesNotInConfiguration() throws IOException {
        config.setPipelines(
                Lists.of(
                        new PipelineConfig("pipeline1", 5),
                        new PipelineConfig("pipeline2", 5),
                        new PipelineConfig("pipeline3", 5)
                )
        ).setDefaultPipelineVersions(10).setPipelinePrefix("");

        when(client.allPipelineNames("")).thenReturn(
                Lists.of("pipeline1", "pipeline2", "pipeline3", "pipeline4")
        );

        List<PipelineConfig> pipelines = janitor().pipelinesNotInConfiguration();

        assertThat(pipelines.size(), is(1));
        assertThat(pipelines.get(0), is(new PipelineConfig("pipeline4", 10)));
    }

    @Test
    public void shouldReturnMandatoryPipelineConfigs() throws IOException {
        List<PipelineConfig> pipelines = Lists.of(
                new PipelineConfig("pipeline1", 2),
                new PipelineConfig("pipeline2", 3)
        );

        when(client.pipelineRunStatus("pipeline1", 0)).thenReturn(pipelineVersions(3, PASSED, 2, PASSED, 1, FAILED));
        when(client.pipelineRunStatus("pipeline2", 0)).thenReturn(pipelineVersions(3, PASSED, 2, PASSED, 1, PASSED));

        List<Tuple2<String, Set<Integer>>> pipelineVersions = janitor().pipelineVersionsToRetain(pipelines);

        assertThat(pipelineVersions.size(), is(2));
        assertThat(pipelineVersions.get(0), is(Tuple2.tuple2("pipeline1", Sets.of(4, 3, 2))));
        assertThat(pipelineVersions.get(1), is(Tuple2.tuple2("pipeline2", Sets.of(4, 3, 2, 1))));
    }

    @Test
    public void shouldComputeWhitelist() throws IOException {
        List<Tuple2<String, Set<Integer>>> pipelineVersions = Lists.of(
                Tuple2.tuple2("pipeline1", Sets.of(4))
        );

        when(client.upstreamDependencies("pipeline1", 4)).thenReturn(Lists.of(
                new PipelineDependency().setPipelineName("pipeline1-Foo").setVersion(4),
                new PipelineDependency().setPipelineName("pipeline1-Bar").setVersion(5),
                new PipelineDependency().setPipelineName("pipeline1-Baz").setVersion(6)
        ));

        WhiteList whiteList = janitor().computeWhiteList(pipelineVersions);

        assertThat(whiteList.pipelinesUnderRadar(), is(Sets.of("pipeline1-Foo", "pipeline1-Bar", "pipeline1-Baz")));
        assertThat(whiteList.versionsForPipeline("pipeline1-Foo"), is(Lists.of(4)));
        assertThat(whiteList.versionsForPipeline("pipeline1-Bar"), is(Lists.of(5)));
        assertThat(whiteList.versionsForPipeline("pipeline1-Baz"), is(Lists.of(6)));

        assertThat(whiteList.contains("pipeline1-Foo", "4"), is(true));
        assertThat(whiteList.contains("pipeline1-Bar", "5"), is(true));
        assertThat(whiteList.contains("pipeline1-Baz", "6"), is(true));
    }

    private Map<Integer, PipelineRunStatus> pipelineVersions(Integer key1, PipelineRunStatus value1,
                                                             Integer key2, PipelineRunStatus value2,
                                                             Integer key3, PipelineRunStatus value3) {
        Maps.MapBuilder<Integer, PipelineRunStatus> builder = Maps.builder();
        builder.put(key1, value1);
        builder.put(key2, value2);
        builder.put(key3, value3);
        return builder.value();
    }


}
