package no.nav.fo.veilarbdialog.db;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static no.nav.fo.veilarbdialog.db.DatabaseContext.AKTIVITET_DATA_SOURCE_JDNI_NAME;
import static org.springframework.util.ReflectionUtils.setField;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import lombok.SneakyThrows;
import no.nav.fo.DatabaseTestContext;
import no.nav.fo.veilarbdialog.db.DatabaseContext;
import no.nav.fo.veilarbdialog.db.dao.DateProvider;

public abstract class DbTest {

    private static AnnotationConfigApplicationContext annotationConfigApplicationContext;
    private static PlatformTransactionManager platformTransactionManager;
    private TransactionStatus transactionStatus;

    protected static void initSpringContext(List<Class> classes) {
        List<Class> list = new ArrayList<>(asList(DbTest.JndiBean.class, DatabaseContext.class, DateProvider.class));
        list.addAll(classes);
        annotationConfigApplicationContext = new AnnotationConfigApplicationContext(list.toArray(new Class[]{}));
        annotationConfigApplicationContext.start();
        platformTransactionManager = annotationConfigApplicationContext.getBean(PlatformTransactionManager.class);
    }

    @Before
    @BeforeEach
    public void setupDateProvider() {
        changeDateProvider(DbTest::timestampFromSystemTime);
    }

    private static String timestampFromSystemTime() {
        return String.format("\'%s\'", new Timestamp(System.currentTimeMillis()));
    }

    @SneakyThrows
    protected void changeDateProvider(Supplier<String> timestampProvider) {
        Field providerField = DateProvider.class.getDeclaredField("provider");
        providerField.setAccessible(true);
        setField(providerField, null, timestampProvider);
    }


    @Component
    public static class JndiBean {

        private final SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();

        public JndiBean() throws Exception {
            builder.bind(AKTIVITET_DATA_SOURCE_JDNI_NAME, DatabaseTestContext.buildMultiDataSource());
            builder.activate();
        }

    }

    @BeforeEach
    @Before
    public void injectAvhengigheter() {
        annotationConfigApplicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @BeforeEach
    @Before
    public void startTransaksjon() {
        transactionStatus = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
    }

    @AfterEach
    @After
    public void rollbackTransaksjon() {
        platformTransactionManager.rollback(transactionStatus);
    }

    @SneakyThrows
    protected Date uniktTidspunkt() {
        sleep(1);
        Date tidspunkt = new Date();
        sleep(1);
        return tidspunkt;
    }

}
