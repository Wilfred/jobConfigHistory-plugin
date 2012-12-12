/*
 * JobConfigChangeBadge.java Nov 21, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package hudson.plugins.jobConfigHistory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildBadgeAction;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

/**
 * This class adds a badge to the build history marking builds 
 * that occurred after the configuration was changed.
 * 
 * @author kstutz
 */
@Extension
public class JobConfigBadgeAction extends RunListener<AbstractBuild> implements BuildBadgeAction {

    /**The logger.*/
    private static final Logger LOG = Logger.getLogger(JobConfigBadgeAction.class.getName());
    
    /**Link to the page that shows the differences between old and new config.*/
    private String linkTarget;

    /**No arguments about a no-argument constructor (necessary because of annotation).*/
    public JobConfigBadgeAction() { }
    
    /**
     * Creates a new JobConfigBadgeAction.
     * @param linkTarget The link target
     */
    public JobConfigBadgeAction(String linkTarget) {
        super(AbstractBuild.class);
        this.linkTarget = linkTarget;
    }

    @Override
    public void onStarted(AbstractBuild build, TaskListener listener) {
        final AbstractProject<?, ?> project = (AbstractProject<?, ?>) build.getProject();
        final Date lastBuildDate = project.getLastBuild().getPreviousBuild().getTime();
        
        //get timestamp of config-change
        final ArrayList<ConfigInfo> configs = new ArrayList<ConfigInfo>();
        final File historyRootDir = Hudson.getInstance().getPlugin(JobConfigHistory.class).getHistoryDir(project.getConfigFile());
        try {
            if (historyRootDir.exists()) {
                for (final File historyDir : historyRootDir.listFiles(JobConfigHistory.HISTORY_FILTER)) {
                    final XmlFile historyXml = new XmlFile(new File(historyDir, JobConfigHistoryConsts.HISTORY_FILE));
                    final HistoryDescr histDescr = (HistoryDescr) historyXml.read();
                    final ConfigInfo config = ConfigInfo.create(project, historyDir, histDescr);
                    configs.add(config);
                }
            }
        } catch (Exception ex) {
            LOG.finest("Could not parse history files: " + ex);
        }

        Collections.sort(configs, ConfigInfoComparator.INSTANCE);
        final ConfigInfo lastChange = Collections.min(configs, ConfigInfoComparator.INSTANCE);
        final ConfigInfo penultimateChange = configs.get(1);
        
        try {
            final Date lastConfigChange = new SimpleDateFormat(JobConfigHistoryConsts.ID_FORMATTER).parse(lastChange.getDate());
            if (lastConfigChange.after(lastBuildDate)) {
                LOG.finest("JJJJJJJJJJJJJJJJJJJJ");
                final String link = createLinkTarget(lastChange, penultimateChange);
                build.addAction(new JobConfigBadgeAction(link));
            }
        } catch (ParseException e) {
            LOG.finest("Could not parse Date: " + e);
        }

        super.onStarted(build, listener);
    }
    
    /**
     * Concatenates strings in order to create the correct link to the showDiffFiles page.
     * @param last newest saved configuration
     * @param secondLast next to newest saved configuration  
     * @return the link
     */
    private String createLinkTarget(ConfigInfo last, ConfigInfo secondLast) {
        return (Jenkins.getInstance().getRootUrl() + "job/" + last.getJob()
            + "/jobConfigHistory/showDiffFiles?histDir1=" + secondLast.getFile()
            + "&histDir2=" + last.getFile());
    }
    
    /**
     * Returns the target for the link to the showDiffFiles page.
     * @return link target as string
     */
    public String getLink() {
        return linkTarget;
    }
    
    /**
     * Returns tooltip so users know what our nice little icon stands for.
     * @return Explanatory text as string
     */
    public String getTooltip() {
        return "Config changed since last build.";
    }

    /**
     * Returns the path to our nice little icon.
     * @return Icon path as string
     */
    public String getIcon() {
        return "/plugin/jobConfigHistory/img/buildbadge.png";
    }

    /**
     * Non-use interface method.
     * {@inheritDoc}
     */
    public String getIconFileName() {
        return null;
    }

    /**
     * Non-use interface method.
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return null;
    }

    /**
     * Non-use interface method.
     * {@inheritDoc}
     */
    public String getUrlName() {
        return "";
    }
}
