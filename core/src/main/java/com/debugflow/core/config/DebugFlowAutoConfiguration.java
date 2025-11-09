package com.debugflow.core.config;

import com.debugflow.core.aspect.FlowAspect;
import com.debugflow.core.async.MdcTaskDecorator;
import com.debugflow.core.event.EventBus;
import com.debugflow.core.exporter.ConsoleJsonExporter;
import com.debugflow.core.exporter.Exporter;
import com.debugflow.core.exporter.PrettyConsoleExporter;
import com.debugflow.core.exporter.FilePrettyExporter;
import com.debugflow.core.session.SessionManager;
import com.debugflow.core.web.SessionController;
import com.debugflow.core.web.TraceContextFilter;
import com.debugflow.core.web.TracePropagation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.TaskDecorator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(DebugFlowProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ConditionalOnClass(FlowAspect.class)
public class DebugFlowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionManager sessionManager(DebugFlowProperties props) {
        SessionManager sm = new SessionManager();
        if (props.isEnabled()) {
            sm.enable(Duration.ofMinutes(props.getTtlMinutes()));
        }
        return sm;
    }

    @Bean
    @ConditionalOnMissingBean
    public EventBus eventBus(ObjectProvider<List<Exporter>> exportersProvider) {
        List<Exporter> exporters = exportersProvider.getIfAvailable(ArrayList::new);
        return new EventBus(exporters);
    }

    @Bean
    @ConditionalOnProperty(prefix = "debugflow", name = "consoleJson", havingValue = "true", matchIfMissing = true)
    public Exporter consoleJsonExporter() { return new ConsoleJsonExporter(); }

    @Bean
    @ConditionalOnProperty(prefix = "debugflow", name = "consolePretty", havingValue = "true")
    public Exporter prettyConsoleExporter(DebugFlowProperties props) { return new PrettyConsoleExporter(props.isColor()); }

    @Bean
    @ConditionalOnProperty(prefix = "debugflow", name = "prettyFile")
    public Exporter filePrettyExporter(DebugFlowProperties props) {
        return new FilePrettyExporter(
                java.nio.file.Path.of(props.getPrettyFile()),
                props.isShowThread(),
                props.isShowHttp(),
                props.isMicros(),
                props.isSimpleClassNames());
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowAspect flowAspect(SessionManager sessionManager,
                                 EventBus eventBus,
                                 @Value("${spring.application.name:unknown-service}") String serviceName,
                                 DebugFlowProperties props) {
        return new FlowAspect(sessionManager, eventBus, serviceName, props.isFollowInboundTraces());
    }

    @Bean
    public FilterRegistrationBean<TraceContextFilter> traceContextFilterRegistration() {
        FilterRegistrationBean<TraceContextFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new TraceContextFilter());
        reg.setOrder(0); // very early
        return reg;
    }

    @Bean
    public SessionController sessionController(SessionManager sm, DebugFlowProperties props) {
        return new SessionController(sm, props.getTtlMinutes());
    }

    @Bean
    @ConditionalOnMissingBean(TaskDecorator.class)
    public TaskDecorator debugflowMdcTaskDecorator() {
        return new MdcTaskDecorator();
    }

    @Bean
    public TracePropagation tracePropagationCustomizer(EventBus eventBus,
                                                       @Value("${spring.application.name:unknown-service}") String serviceName) {
        return new TracePropagation(eventBus, serviceName);
    }
}
