package com.die_macher;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {

	ApplicationModules modules = ApplicationModules.of(System1Application.class);

	@Test
	void verifiesModularStructure() {
		modules.verify();
	}
}
