package com.djonique.cashbackadvbot;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("integration")
@ActiveProfiles("local")
@SpringBootTest(properties = {
		"telegrambots.enabled=false",
		"app.telegram.registration-enabled=false",
		"app.telegram.bot-token=test-token",
		"app.telegram.bot-username=TestCashbackBot",
		"app.telegram.payments.provider-token=test-provider-token"
})
class CashbackAdvisorBotApplicationTests {

	@Test
	void contextLoads() {
	}

}
