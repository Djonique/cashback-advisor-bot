package com.djonique.cashbackadvbot;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("integration")
@SpringBootTest(properties = {
		"telegrambots.enabled=false",
		"app.telegram.registration-enabled=false",
		"app.telegram.bot-token=test-token",
		"app.telegram.bot-username=TestCashbackBot"
})
class CashbackAdvisorBotApplicationTests {

	@Test
	void contextLoads() {
	}

}
