package laba1;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;

import org.python.util.PythonInterpreter;
import org.python.core.*;

public class Main {
    public static void main(String[] args) throws PyException {
    	PythonInterpreter pi = new PythonInterpreter();
    	pi.exec("from profanityfilter import ProfanityFilter");
    	pi.exec("pf = ProfanityFilter()");
        pi.set("string", new PyString("F U	C K you"));
        pi.exec("result = pf.censor(string)");
        pi.exec("print(result)");
        PyString result = (PyString)pi.get("result");
        System.out.println("result: "+ result.asString());
    	
    	
        PlatformConfiguration   config  = PlatformConfiguration.getDefault();

        config.setDefaultTimeout(0);
        config.addComponent("laba1.BotAgent.class");
        config.addComponent("laba1.RegistryAgent.class");
        config.addComponent("laba1.ChatAgent.class");
        config.addComponent("laba1.ChatAgent.class");
        config.addComponent("laba1.ChatAgent.class");
        Starter.createPlatform(config).get();
    }
}