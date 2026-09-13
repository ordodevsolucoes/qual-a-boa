package br.com.ordodev.qualaboa.configuracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ConfiguracaoJwtTest {

	private ConfiguracaoJwt comSegredo(String segredo) {
		ConfiguracaoJwt configuracao = new ConfiguracaoJwt();
		ReflectionTestUtils.setField(configuracao, "segredo", segredo);
		return configuracao;
	}

	@Test
	void falhaAoSubirComSegredoAusente() {
		assertThatThrownBy(() -> comSegredo("").chaveJwt())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("32 bytes");
	}

	@Test
	void falhaAoSubirComSegredoMenorQue32Bytes() {
		assertThatThrownBy(() -> comSegredo("segredo-curto").chaveJwt())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("32 bytes");
	}

	@Test
	void aceitaSegredoComExatamente32Bytes() {
		assertThat(comSegredo("a".repeat(32)).chaveJwt()).isNotNull();
	}

}
