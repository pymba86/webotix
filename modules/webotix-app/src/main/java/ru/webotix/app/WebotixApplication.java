package ru.webotix.app;

import com.google.inject.Module;

public class WebotixApplication extends WebHostApplication {

    public static void main(String... args) throws Exception {
        new WebotixApplication().run(args);
    }

    @Override
    public String getName() {
        return "Webotix";
    }

    @Override
    protected Module createApplicationModule() {
        return new WebotixModule();
    }
}
