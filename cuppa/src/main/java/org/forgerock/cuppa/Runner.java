/*
 * Copyright 2015-2016 ForgeRock AS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.forgerock.cuppa;

import static org.forgerock.cuppa.model.TestBlockType.ROOT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.forgerock.cuppa.internal.TestBlockRunner;
import org.forgerock.cuppa.internal.TestContainer;
import org.forgerock.cuppa.internal.filters.EmptyTestBlockFilter;
import org.forgerock.cuppa.internal.filters.OnlyTestBlockFilter;
import org.forgerock.cuppa.internal.filters.TagTestBlockFilter;
import org.forgerock.cuppa.model.Tags;
import org.forgerock.cuppa.model.TestBlock;
import org.forgerock.cuppa.model.TestBlockBuilder;
import org.forgerock.cuppa.reporters.CompositeReporter;
import org.forgerock.cuppa.reporters.Reporter;

/**
 * Runs Cuppa tests.
 */
public final class Runner {
    private static final TestBlock EMPTY_TEST_BLOCK = new TestBlockBuilder()
            .setType(ROOT)
            .setTestClass(Cuppa.class)
            .setDescription("")
            .build();

    private final List<Function<TestBlock, TestBlock>> coreTestTransforms;
    private final Configuration configuration;

    /**
     * Creates a new runner with no run tags and a configuration loaded from the classpath.
     *
     * @param configuration Cuppa configuration to control the behaviour of the runner.
     */
    public Runner(Configuration configuration) {
        this(Tags.EMPTY_TAGS, configuration);
    }

    /**
     * Creates a new runner with the given run tags and configuration.
     *
     * @param runTags Tags to filter the tests on.
     * @param configuration Cuppa configuration to control the behaviour of the runner.
     */
    public Runner(Tags runTags, Configuration configuration) {
        coreTestTransforms = Arrays.asList(new OnlyTestBlockFilter(), new TagTestBlockFilter(runTags),
                new EmptyTestBlockFilter());
        this.configuration = configuration;
    }

    /**
     * Instantiates the test classes, which define tests as side effects, and return the root test block.
     *
     * @param testClasses The test classes that contain the tests to be executed.
     * @return The root block that contains all other test blocks and their tests.
     */
    public TestBlock defineTests(Iterable<Class<?>> testClasses) {
        return defineTestsWithConfiguration(testClasses, configuration.testInstantiator);
    }

    /**
     * Runs the tests contained in the provided test block and any nested test blocks, using the provided reporter.
     *
     * @param rootBlock The root test block that contains all tests to be run.
     * @param reporter The reporter to use to report test results.
     */
    public void run(TestBlock rootBlock, Reporter reporter) {
        Reporter fullReporter = (configuration.additionalReporter != null)
                ? new CompositeReporter(Arrays.asList(reporter, configuration.additionalReporter))
                : reporter;
        TestContainer.INSTANCE.runTests(() -> {
            fullReporter.start(rootBlock);
            TestBlock transformedRootBlock = transformTests(rootBlock, configuration.testTransforms);
            runTests(transformedRootBlock, fullReporter);
            fullReporter.end();
        });
    }

    private TestBlock defineTestsWithConfiguration(Iterable<Class<?>> testClasses, TestInstantiator testInstantiator) {
        return StreamSupport.stream(testClasses.spliterator(), false)
                .map(c -> TestContainer.INSTANCE.defineTests(c, () -> {
                    try {
                        testInstantiator.instantiate(c);
                    } catch (CuppaException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to instantiate test class: " + c.getName(), e);
                    }
                }))
                .reduce(EMPTY_TEST_BLOCK, this::mergeRootTestBlocks);
    }

    private TestBlock mergeRootTestBlocks(TestBlock testBlock1, TestBlock testBlock2) {
        return EMPTY_TEST_BLOCK.toBuilder().setTestBlocks(Stream.concat(testBlock1.testBlocks.stream(),
                testBlock2.testBlocks.stream()).collect(Collectors.toList())).build();
    }

    private TestBlock transformTests(TestBlock rootBlock, List<Function<TestBlock, TestBlock>> transforms) {
        return Stream.concat(transforms.stream(), coreTestTransforms.stream())
                .reduce(Function.identity(), Function::andThen)
                .apply(rootBlock);
    }

    private void runTests(TestBlock rootBlock, Reporter reporter) {
        TestBlockRunner rootRunner = createRunner(rootBlock, Collections.emptyList(), reporter);
        rootRunner.run();
    }

    private TestBlockRunner createRunner(TestBlock testBlock, List<TestBlockRunner> parents, Reporter reporter) {
        TestBlockRunner runner = new TestBlockRunner(testBlock, parents, reporter);
        List<TestBlockRunner> newParents = Stream.concat(parents.stream(), Stream.of(runner))
                .collect(Collectors.toList());
        for (TestBlock nestedBlock : testBlock.testBlocks) {
            TestBlockRunner child = createRunner(nestedBlock, newParents, reporter);
            runner.addChild(child);
        }
        return runner;
    }
}
