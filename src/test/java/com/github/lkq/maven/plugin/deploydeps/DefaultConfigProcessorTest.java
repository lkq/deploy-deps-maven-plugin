package com.github.lkq.maven.plugin.deploydeps;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultConfigProcessorTest {
    DefaultConfigProcessor processor = new DefaultConfigProcessor();

    @Mock
    private DefaultConfig config1;
    @Mock
    private DefaultConfig config2;

    @Test
    public void willPassWithValidConfig() throws Exception {

        setupValidMock(config1);
        setupValidMock(config2);
        processor.process(Arrays.asList(config1, config2));

        verify(config1, times(1)).setPort("22");
        verify(config1, times(1)).setTargetFileMode("0640");
        verify(config2, times(1)).setPort("22");
        verify(config2, times(1)).setTargetFileMode("0640");
    }

    @Test
    public void willFailWithMissingUser() throws Exception {
        setupValidMock(config1);
        String errMsg = null;
        // missing user
        try {
            processor.process(Arrays.asList(config1, config2));
        } catch (RuntimeException e) {
            errMsg = e.getMessage();
        }
        assertThat(errMsg, is("missing ssh user"));

        // missing host
        try {
            given(config2.getUser()).willReturn("user");
            processor.process(Arrays.asList(config2));
        } catch (RuntimeException e) {
            errMsg = e.getMessage();
        }
        assertThat(errMsg, is("missing ssh host"));

        // missing key file
        try {
            given(config2.getUser()).willReturn("user");
            given(config2.getHost()).willReturn("host");
            processor.process(Arrays.asList(config2));
        } catch (RuntimeException e) {
            errMsg = e.getMessage();
        }
        assertThat(errMsg, is("missing ssh key file"));

        // missing target path
        try {
            given(config2.getUser()).willReturn("user");
            given(config2.getHost()).willReturn("host");
            given(config2.getKeyFile()).willReturn("key file");
            processor.process(Arrays.asList(config2));
        } catch (RuntimeException e) {
            errMsg = e.getMessage();
        }
        assertThat(errMsg, is("missing target path"));
    }

    private void setupValidMock(DefaultConfig mockConfig) {
        given(mockConfig.getUser()).willReturn("user");
        given(mockConfig.getHost()).willReturn("host");
        given(mockConfig.getKeyFile()).willReturn("keyFile");
        given(mockConfig.getTargetPath()).willReturn("targetPath");
    }
}