package org.jenkinsci.plugins.workflow.multibranch;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.Run;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchPropertyDescriptor;
import jenkins.branch.JobDecorator;
import org.jenkinsci.plugins.workflow.flow.FlowDurabilityHint;
import org.jenkinsci.plugins.workflow.flow.GlobalDefaultFlowDurabilityLevel;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.properties.DurabilityHintJobProperty;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

/**
 * Branch property so we can define per-branch durability policies, i.e. so feature branches aren't built durably but master is.
 * Also lets us set the durability level before the pipeline has run (a step ahead of the "properties" step).
 * @author Sam Van Oort
 */
@Restricted(NoExternalUse.class)
public class DurabilityHintBranchProperty extends BranchProperty {

    private final FlowDurabilityHint hint;

    public FlowDurabilityHint getHint() {
        return hint;
    }

    @DataBoundConstructor
    public DurabilityHintBranchProperty(@Nonnull FlowDurabilityHint hint) {
        this.hint = hint;
    }

    @Override
    public final <P extends Job<P, B>, B extends Run<P, B>> JobDecorator<P, B> jobDecorator(Class<P> clazz) {
        if (!clazz.isAssignableFrom(WorkflowJob.class)) {
            return null;
        }
        return new JobDecorator<P, B>() {
            @NonNull
            @Override
            public List<JobProperty<? super P>> jobProperties(
                    @NonNull List<JobProperty<? super P>> jobProperties) {
                List<JobProperty<? super P>> result = asArrayList(jobProperties);
                for (Iterator<JobProperty<? super P>> iterator = result.iterator(); iterator.hasNext(); ) {
                    JobProperty<? super P> p = iterator.next();
                    if (p instanceof DurabilityHintJobProperty) {
                        iterator.remove();
                    }
                }
                if (getHint() != null) {
                    result.add((JobProperty)(new DurabilityHintJobProperty(getHint())));
                }
                return result;
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends BranchPropertyDescriptor {
        @Override
        public String getDisplayName() {
            return "Pipeline speed/durability override";
        }

        public FlowDurabilityHint[] getDurabilityHintValues() {
            return FlowDurabilityHint.values();
        }

        public static FlowDurabilityHint getDefaultDurabilityHint() {
            return GlobalDefaultFlowDurabilityLevel.getDefaultDurabilityHint();
        }
    }
}
