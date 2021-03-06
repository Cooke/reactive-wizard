package se.fortnox.reactivewizard.config;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTestInjector {


    @Test
    public void testCreateWithOutConfigFile() {
        Injector injector = TestInjector.create();
        assertThat(injector.getInstance(ConfigFactory.class).getClass().getName()).isNotEqualTo(ConfigFactory.class.getName());

        String[] args = injector.getInstance(Key.get(String[].class, Names.named("args")));
        assertThat(args.length).isEqualTo(0);
    }

    @Test
    public void testCreateWithConfigFile() {
        Injector injector = TestInjector.create("src/test/resources/testconfig.yml");
        assertThat(injector.getInstance(ConfigAutoBindModule.class).getClass().getName()).isEqualToIgnoringCase(ConfigAutoBindModule.class.getName());
        assertThat(injector.getInstance(ConfigFactory.class).getClass().getName()).isEqualTo(ConfigFactory.class.getName());

        String[] args = injector.getInstance(Key.get(String[].class, Names.named("args")));
        assertThat(args.length).isEqualTo(1);
        assertThat(args[0]).isEqualTo("src/test/resources/testconfig.yml");
    }
}
