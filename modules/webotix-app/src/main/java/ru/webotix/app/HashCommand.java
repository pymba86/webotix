package ru.webotix.app;

import io.dropwizard.cli.Cli;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import ru.webotix.auth.Hasher;

class HashCommand extends Command {

    private static final String SALT_PARM = "salt";
    private static final String VALUE_PARM = "value";

    HashCommand() {
        super("hash", "Hashes the specified value using a provided salt");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        Hasher hasher = new Hasher();
        String salt = namespace.getString(SALT_PARM);
        if (salt == null) {
            salt = hasher.salt();
            System.out.println("Salt used: " + salt);
            System.out.println("Hashed result: " + hasher.hash(namespace.getString(VALUE_PARM), salt));
        } else {
            System.out.print(hasher.hash(namespace.getString(VALUE_PARM), salt));
        }
    }

    @Override
    public void configure(Subparser subparser) {
        subparser
                .addArgument("--" + SALT_PARM, "-s")
                .help("An encryption salt. If not provided, a new one will be used and returned.");
        subparser.addArgument(VALUE_PARM).required(true).help("The value for which to create a hash.");
    }

    @Override
    public void onError(Cli cli, Namespace namespace, Throwable e) {
        cli.getStdErr().println(e.getMessage());
    }
}
