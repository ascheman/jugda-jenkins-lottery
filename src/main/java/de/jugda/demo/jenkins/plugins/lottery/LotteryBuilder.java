package de.jugda.demo.jenkins.plugins.lottery;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link LotteryBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #fileName})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Gerd Aschemann
 */
public class LotteryBuilder extends Builder {

    private final String fileName;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public LotteryBuilder(String fileName) {
        this.fileName = fileName;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return fileName;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException {
        // This is where you 'build' the project.

    	PrintStream logger = listener.getLogger();
		logger.println("Using attendees file È" + fileName + "Ç!");
//    	BufferedReader attendeesFile = new BufferedReader(new FileReader(fileName, "ISO-8859-15"));
		BufferedReader attendeesFile = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(fileName
//								new FileReader(fileName)
								)
						, Charset.forName("ISO-8859-15")
						)
				);
    	logger.println("The following people registered for the event:");
    	List<String> attendeesList = new ArrayList<String>();
    	String line;
    	while ((line = attendeesFile.readLine()) != null) {
    		attendeesList.add(line);
    	}
    	attendeesFile.close();
    	for (String attendee : attendeesList) {
    		logger.println("\t" + attendee);
    	}
    	Random rn = new Random();
    	int maximum = attendeesList.size();
    	int winnerNumber = Math.abs(rn.nextInt()) % maximum;
    	logger.println("Chose #" + winnerNumber + " (out of " + maximum + ")");
    	String winner = attendeesList.get(winnerNumber);
    	logger.println("THE WINNER IS: '" + winner + "'");
    	
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link LotteryBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/de/jugda/demo/jenkins/plugins/lottery/LotteryBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */

        /**
         * Performs on-the-fly validation of the form field 'fileName'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a fileName");
            if (!value.startsWith("/")) {
            	return FormValidation.error("FileName should start with an '/'");
            }
            if (!new File(value).exists()) {
            	return FormValidation.error("File Ç" + value + "È does not exist!");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable fileName is used in the configuration screen.
         */
        public String getDisplayName() {
            return "JUG DA Lottery";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }
    }
}

