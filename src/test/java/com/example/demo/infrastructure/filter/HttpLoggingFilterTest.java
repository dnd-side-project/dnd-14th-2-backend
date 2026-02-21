package com.example.demo.infrastructure.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpLoggingFilterTest {

    private HttpLoggingFilter filter;
    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        filter = new HttpLoggingFilter();

        logger = (Logger) LoggerFactory.getLogger(HttpLoggingFilter.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
        MDC.clear();
    }

    @Test
    void 요청과_응답을_로깅한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ledger");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        List<ILoggingEvent> logs = logAppender.list;
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getFormattedMessage()).contains("[REQUEST]", "GET", "/api/ledger");
        assertThat(logs.get(1).getFormattedMessage()).contains("[RESPONSE]", "GET", "/api/ledger", "status=200");
    }

    @Test
    void 요청에_requestId를_부여하고_응답_헤더에_포함한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ledger");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        String requestId = response.getHeader("X-Request-Id");
        assertThat(requestId).isNotNull().hasSize(8);
    }

    @Test
    void 요청_처리_중에는_MDC에_requestId가_존재해야_한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ledger");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // filterChain이 실행되는 도중에 MDC 값을 검증
        FilterChain filterChain = (req, res) -> {
            assertThat(MDC.get("requestId")).isNotNull().hasSize(8);
        };

        // when
        filter.doFilter(request, response, filterChain);
    }

    @Test
    void 필터_완료_후_MDC가_정리된다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ledger");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void 쿼리스트링을_로깅한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ledger");
        request.setQueryString("month=2026-02");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        String requestLog = logAppender.list.get(0).getFormattedMessage();
        assertThat(requestLog).contains("query=month=2026-02");
    }

    @Test
    void 클라이언트_에러_응답은_WARN_레벨로_본문을_포함하여_로깅한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ledger");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(400);
            res.getWriter().write("{\"message\":\"invalid amount\"}");
        };

        // when
        filter.doFilter(request, response, filterChain);

        // then
        ILoggingEvent responseLog = logAppender.list.get(1);
        assertThat(responseLog.getLevel()).isEqualTo(Level.WARN);
        assertThat(responseLog.getFormattedMessage()).contains("status=400", "invalid amount");
    }

    @Test
    void 서버_에러_응답은_ERROR_레벨로_본문을_포함하여_로깅한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ledger");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(500);
            res.getWriter().write("{\"message\":\"server error\"}");
        };

        // when
        filter.doFilter(request, response, filterChain);

        // then
        ILoggingEvent responseLog = logAppender.list.get(1);
        assertThat(responseLog.getLevel()).isEqualTo(Level.ERROR);
        assertThat(responseLog.getFormattedMessage()).contains("status=500", "server error");
    }

    @Test
    void 정상_응답은_INFO_레벨로_본문_없이_로깅한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ledger");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        ILoggingEvent responseLog = logAppender.list.get(1);
        assertThat(responseLog.getLevel()).isEqualTo(Level.INFO);
        assertThat(responseLog.getFormattedMessage()).doesNotContain("body=");
    }

    @Test
    void 응답_본문이_1000자를_초과하면_잘린다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ledger");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String longBody = "a".repeat(2000);
        FilterChain filterChain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(500);
            res.getWriter().write(longBody);
        };

        // when
        filter.doFilter(request, response, filterChain);

        // then
        String responseLog = logAppender.list.get(1).getFormattedMessage();
        assertThat(responseLog).contains("...(truncated)");
        assertThat(responseLog).doesNotContain(longBody);
    }

    @Test
    void actuator_경로는_필터링하지_않는다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");

        // when & then
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void swagger_경로는_필터링하지_않는다() {
        // given
        MockHttpServletRequest swaggerUi = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletRequest apiDocs = new MockHttpServletRequest("GET", "/v3/api-docs/swagger-config");

        // when & then
        assertThat(filter.shouldNotFilter(swaggerUi)).isTrue();
        assertThat(filter.shouldNotFilter(apiDocs)).isTrue();
    }

    @Test
    void API_경로는_필터링한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ledger");

        // when & then
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void 예외_발생_시에도_MDC가_정리된다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ledger");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            throw new RuntimeException("unexpected error");
        };

        // when & then
        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
            .isInstanceOf(RuntimeException.class);
        assertThat(MDC.get("requestId")).isNull();
    }
}
