/*
 * Copyright (c) 2012-2013 by Jens V. Fischer, Zuse Institute Berlin
 *
 *
 * Licensed under the BSD License, see LICENSE file for details.
 *
 */

package org.xtreemfs.utils.xtfs_benchmark;

import static org.xtreemfs.foundation.util.CLIParser.CliOption.OPTIONTYPE.STRING;
import static org.xtreemfs.foundation.util.CLIParser.CliOption.OPTIONTYPE.SWITCH;
import static org.xtreemfs.foundation.util.CLIParser.parseCLI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xtreemfs.common.benchmark.BenchmarkUtils;
import org.xtreemfs.foundation.util.CLIParser;
import org.xtreemfs.common.benchmark.Config;
import org.xtreemfs.common.benchmark.ConfigBuilder;
import org.xtreemfs.common.benchmark.Controller;
import org.xtreemfs.utils.DefaultDirConfig;
import org.xtreemfs.utils.utils;

/**
 * Class implementing the commandline options for {@link xtfs_benchmark}.
 * 
 * @author jensvfischer
 */
class CLIOptions {
    private Map<String, CLIParser.CliOption> options;
    private List<String>                     arguments;
    private ConfigBuilder builder;

    private static final String              DIR_ADDRESS;
    private static final String              OSD_SELECTION_POLICIES;
    private static final String              OSD_SELECTION_UUIDS;
    private static final String              USERNAME;
    private static final String              GROUPNAME;
    private static final String              SEQ_WRITE;
    private static final String              SEQ_READ;
    private static final String              RAND_WRITE;
    private static final String              RAND_READ;
    private static final String              FILEBASED_WRITE;
    private static final String              FILEBASED_READ;
    private static final String              THREADS;
    private static final String              REPETITIONS;
    private static final String              CHUNK_SIZE;
    private static final String              STRIPE_SIZE;
    private static final String              STRIPE_WITDH;
    private static final String              SIZE_SEQ;
    private static final String              SIZE_RAND;
    private static final String              SIZE_BASEFILE;
    private static final String              SIZE_FILES;
    private static final String              NO_CLEANUP;
    private static final String              NO_CLEANUP_VOLUMES;
    private static final String              NO_CLEANUP_BASEFILE;
    private static final String              OSD_CLEANUP;

    static {
        DIR_ADDRESS = "-dir-address";
        OSD_SELECTION_POLICIES = "-osd-selection-policies";
        OSD_SELECTION_UUIDS = "-osd-selection-uuids";
        USERNAME = "-user";
        GROUPNAME = "-group";
        SEQ_WRITE = "sw";
        SEQ_READ = "sr";
        RAND_WRITE = "rw";
        RAND_READ = "rr";
        FILEBASED_WRITE = "fw";
        FILEBASED_READ = "fr";
        THREADS = "t";
        REPETITIONS = "r";
        CHUNK_SIZE = "-chunk-size";
        STRIPE_SIZE = "-stripe-size";
        STRIPE_WITDH = "-stripe-width";
        SIZE_SEQ = "ssize";
        SIZE_RAND = "rsize";
        SIZE_BASEFILE = "-basefile-size";
        SIZE_FILES = "-file-size";
        NO_CLEANUP = "-no-cleanup";
        NO_CLEANUP_VOLUMES = "-no-cleanup-volumes";
        NO_CLEANUP_BASEFILE = "-no-cleanup-basefile";
        OSD_CLEANUP = "-osd-cleanup";
    }

    CLIOptions() {
        this.options = utils.getDefaultAdminToolOptions(true);
        this.builder = new ConfigBuilder();
        this.arguments = new ArrayList<String>(20);
    }

    void parseCLIOptions(String[] args) {
        initOptions();
        parseCLI(args, options, arguments);
    }

    Config buildParamsFromCLIOptions() throws Exception {
        setBasefileSize();
        setFileSize();
        setDirAddress();
        setOsdSelectionPolicies();
        setOsdSelectionByUuids();
        setUsername();
        setGroup();
        setOSDPassword();
        setAuth();
        setSSLOptions();
        setOptions();
        setChunkSize();
        setStripeSize();
        setStripeWidth();
        setNoCleanup();
        setNoCleanupOfVolumes();
        setNoCleanupOfBasefile();
        setOsdCleanup();
        return builder.build();
    }

    private void initOptions() {

        /* Connection Data */
        options.put(DIR_ADDRESS, new CLIParser.CliOption(STRING,
                "directory service to use (e.g. 'localhost:32638'). If no URI is specified, URI and security settings are taken from '"
                        + DefaultDirConfig.DEFAULT_DIR_CONFIG + "'", "<uri>"));
        options.put(OSD_SELECTION_POLICIES, new CLIParser.CliOption(STRING,
                "OSD selection policies to use when creating or opening volumes. default: 1000,3002",
                "<osd selection policies>"));
        options.put(OSD_SELECTION_UUIDS, new CLIParser.CliOption(STRING,
                        "Set the UUID-based filter policy (ID 1002) as OSD selection policy and set the uuids to be used " +
                                "by the policy (applied when creating or opening volumes). It is not allowed to use this option " +
                                "with the -" + OSD_SELECTION_POLICIES + " option. Default: No selection by UUIDs", "<osd uuids to use>"));
        options.put(USERNAME, new CLIParser.CliOption(STRING, "username to use", "<username>"));
        options.put(GROUPNAME, new CLIParser.CliOption(STRING, "name of group to use", "<group name>"));

        /* benchmark switches */
        options.put(SEQ_WRITE, new CLIParser.CliOption(SWITCH, "sequential write benchmark", ""));
        options.put(SEQ_READ, new CLIParser.CliOption(SWITCH, "sequential read benchmark", ""));
        options.put(RAND_WRITE, new CLIParser.CliOption(SWITCH, "random write benchmark", ""));
        options.put(RAND_READ, new CLIParser.CliOption(SWITCH, "random read benchmark", ""));
        options.put(FILEBASED_WRITE, new CLIParser.CliOption(SWITCH, "random filebased write benchmark", ""));
        options.put(FILEBASED_READ, new CLIParser.CliOption(SWITCH, "random filebased read benchmark", ""));

        options.put(THREADS, new CLIParser.CliOption(STRING,
                "number of benchmarks to be started in parallel. default: 1", "<number>"));
        options.put(REPETITIONS, new CLIParser.CliOption(STRING, "number of repetitions of a benchmarks. default: 1",
                "<number>"));
        options.put(CHUNK_SIZE, new CLIParser.CliOption(STRING,
                "Chunk size of reads/writes in benchmark in [B|K|M|G] (no modifier assumes bytes). default: 128K", "<chunkSize>"));
        options.put(STRIPE_SIZE, new CLIParser.CliOption(STRING,
                "stripeSize in [B|K|M|G] (no modifier assumes bytes). default: 128K", "<stripeSize>"));
        options.put(STRIPE_WITDH, new CLIParser.CliOption(STRING, "stripe width. default: 1", "<stripe width>"));

        /* sizes */
        options.put(SIZE_SEQ, new CLIParser.CliOption(STRING,
                "size for sequential benchmarks in [B|K|M|G] (no modifier assumes bytes)", "<size>"));
        options.put(SIZE_RAND, new CLIParser.CliOption(STRING,
                "size for random benchmarks in [B|K|M|G] (no modifier assumes bytes)", "<size>"));
        options.put(SIZE_BASEFILE, new CLIParser.CliOption(STRING,
                "size of the basefile for random benchmarks in [B|K|M|G] (no modifier assumes bytes)", "<size>"));
        options.put(SIZE_FILES, new CLIParser.CliOption(STRING,
                "size of the files for filebased benchmarks in [B|K|M|G] (no modifier assumes bytes)."
                        + " The filesize must be <= 2^31-1", "<size>"));

        /* deletion options */
        String noCleanupDescription = "do not delete created volumes and files. Volumes and files need to be removed "
                + "manually. Volumes can be removed using rmfs.xtreemfs. Files can be removed by mounting the according "
                + "volume with mount.xtreemfs and deleting the files with rm";
        options.put(NO_CLEANUP, new CLIParser.CliOption(SWITCH, noCleanupDescription, ""));

        options.put(NO_CLEANUP_VOLUMES, new CLIParser.CliOption(SWITCH,
                "do not delete created volumes. Created volumes neet to be removed manually using rmfs.xtreemfs", ""));

        String noCleanupBasefileDescription = "do not delete created basefile (only works with --no-cleanup or "
                + "--no-cleanup-volumes. Created Files and volumes need to be removed manually";
        options.put(NO_CLEANUP_BASEFILE, new CLIParser.CliOption(SWITCH, noCleanupBasefileDescription, ""));
        options.put(OSD_CLEANUP, new CLIParser.CliOption(SWITCH, "Run OSD cleanup after the benchmarks", ""));
    }

    boolean usageIsSet() {
        return (options.get(utils.OPTION_HELP).switchValue || options.get(utils.OPTION_HELP_LONG).switchValue);
    }

    /**
     * Prints out displayUsage informations
     */
    void displayUsage() {

        System.out.println("\nusage: xtfs_benchmark [options] volume1 volume2 ... \n");
        System.out
                .println("The number of volumes must be in accordance with the number of benchmarks run in parallel (see -p).");
        System.out
                .println("All sizes can be modified with multiplication modifiers, where K means KiB, M means MiB and G means GiB. \n"
                        + "If no modifier is given, sizes are assumed to be in bytes.");
        System.out.println();
        System.out.println("  " + "options:");
        utils.printOptions(options);
        System.out.println();
        System.out.println("example: xtfs_benchmark -sw -sr -t 3 -ssize 3G volume1 volume2 volume3");
        System.out
                .println("\t\t starts a sequential write and read benchmark of 3 GiB with 3 benchmarks in parallel on volume1, volume2 and volume3\n");
    }

    private void setBasefileSize() {
        String optionValue;
        optionValue = options.get(SIZE_BASEFILE).stringValue;
        if (null != optionValue)
            builder.setBasefileSizeInBytes(parseSizeWithModifierToBytes(optionValue));
    }

    private void setFileSize() {
        String optionValue;
        optionValue = options.get(SIZE_FILES).stringValue;
        if (null != optionValue) {
            long sizeInBytes = parseSizeWithModifierToBytes(optionValue);
            if (sizeInBytes > Integer.MAX_VALUE)
                throw new IllegalArgumentException("Filesize for filebased random IO Benchmarks must be <= 23^31-1");
            builder.setFilesize((int) sizeInBytes);
        }
    }

    /* if no DirAdress is given, use DirAddress from ConfigFile */
    private void setDirAddress() {
        String dirAddressFromCLI = options.get(DIR_ADDRESS).stringValue;
        String dirAddressFromConfig = Controller.getDefaultDir();
        if (null != dirAddressFromCLI)
            builder.setDirAddress(dirAddressFromCLI);
        else if (null != dirAddressFromConfig)
            builder.setDirAddress(dirAddressFromConfig);
    }

    private void setOsdSelectionPolicies() {
        String osdSelectionPolicies = options.get(OSD_SELECTION_POLICIES).stringValue;
        if (null != osdSelectionPolicies)
            builder.setOsdSelectionPolicies(osdSelectionPolicies);
    }

    private void setOsdSelectionByUuids() {
        String osdSelectionUuids = options.get(OSD_SELECTION_UUIDS).stringValue;
        if (null != osdSelectionUuids)
            builder.setSelectOsdsByUuid(osdSelectionUuids);
    }

    private void setUsername() {
        String userName = options.get(USERNAME).stringValue;
        if (null != userName)
            builder.setUserName(userName);
    }

    private void setGroup() {
        /* if no group option is given, use username as groupname */
        String groupName = options.get(GROUPNAME).stringValue;
        if (null != groupName)
            builder.setGroup(groupName);
        else {
            String userName = options.get(USERNAME).stringValue;
            if (null != userName)
                builder.setGroup(userName);
        }
    }

    private void setOSDPassword() {
        String osdPassword = options.get(utils.OPTION_ADMIN_PASS).stringValue;
        if (null != osdPassword)
            builder.setAdminPassword(osdPassword);
    }

    private void setAuth() {
        // Todo (jvf) implement?
    }

    private void setSSLOptions() {
        // Todo (jvf) Implement SSL Options?
    }

    private void setOptions() {
        // Todo (jvf) implement?
    }

    private void setChunkSize() {
        String chunkSize = options.get(CHUNK_SIZE).stringValue;
        if (null != chunkSize){
            long chunkSizeInBytes = parseSizeWithModifierToBytes(chunkSize);
            assert chunkSizeInBytes <= Integer.MAX_VALUE : "ChunkSize must be less equal than Integer.MAX_VALUE";
            builder.setChunkSizeInBytes((int)chunkSizeInBytes);
        }
    }

    private void setStripeSize() {
        String stripeSize = options.get(STRIPE_SIZE).stringValue;
        if (null != stripeSize) {
            long stripeSizeInBytes = parseSizeWithModifierToBytes(stripeSize);
            assert stripeSizeInBytes <= Integer.MAX_VALUE : "StripeSize must be less equal than Integer.MAX_VALUE";
            builder.setStripeSizeInBytes((int) stripeSizeInBytes);
        }
    }

    private void setStripeWidth() {
        String stripeWidth = options.get(STRIPE_WITDH).stringValue;
        if (null != stripeWidth)
            builder.setStripeWidth(Integer.parseInt(stripeWidth));
    }

    private void setNoCleanup() {
        boolean switchValue = options.get(NO_CLEANUP).switchValue;
        if (switchValue)
            builder.setNoCleanup();
    }

    private void setNoCleanupOfVolumes() {
        boolean switchValue = options.get(NO_CLEANUP_VOLUMES).switchValue;
        if (switchValue)
            builder.setNoCleanupOfVolumes();
    }

    private void setNoCleanupOfBasefile() {
        boolean switchValue = options.get(NO_CLEANUP_BASEFILE).switchValue;
        if (switchValue)
            builder.setNoCleanupOfBasefile();
    }

    private void setOsdCleanup() {
        boolean switchValue = options.get(OSD_CLEANUP).switchValue;
        if (switchValue)
            builder.setOsdCleanup();
    }

    int getNumberOfThreads() {
        String optionValue = options.get(THREADS).stringValue;
        if (null != optionValue)
            return Integer.valueOf(optionValue);
        else
            return 1; // default value
    }

    long getSequentialSize() {
        String optionValue;
        optionValue = options.get(SIZE_SEQ).stringValue;
        if (null != optionValue)
            return parseSizeWithModifierToBytes(optionValue);
        else
            return 10L*BenchmarkUtils.MiB_IN_BYTES; // default value
    }

    long getRandomSize() {
        String optionValue;
        optionValue = options.get(SIZE_RAND).stringValue;
        if (null != optionValue)
            return parseSizeWithModifierToBytes(optionValue);
        else
            return 10L*BenchmarkUtils.MiB_IN_BYTES; // default value
    }

    int getNumberOfRepetitions() {
        String optionValue = options.get(REPETITIONS).stringValue;
        if (null != optionValue)
            return Integer.valueOf(optionValue);
        else
            return 1; // default value
    }

    boolean sequentialWriteBenchmarkIsSet() {
        return options.get(SEQ_WRITE).switchValue;
    }

    boolean sequentialReadBenchmarkIsSet() {
        return options.get(SEQ_READ).switchValue;
    }

    boolean randomReadBenchmarkIsSet() {
        return options.get(RAND_READ).switchValue;
    }

    boolean randomWriteBenchmarkIsSet() {
        return options.get(RAND_WRITE).switchValue;
    }

    boolean randomFilebasedWriteBenchmarkIsSet() {
        return options.get(FILEBASED_WRITE).switchValue;
    }

    boolean randomFilebasedReadBenchmarkIsSet() {
        return options.get(FILEBASED_READ).switchValue;
    }

    private long parseSizeWithModifierToBytes(String size) {
        size = size.toUpperCase();
        long sizeInBytes;

        if (!size.matches("[0-9]+[BKMG]?"))
            throw new IllegalArgumentException("Wrong format for size.");

        if (size.matches("[0-9]+")) {
            sizeInBytes = Long.valueOf(size);
        } else {
            char suffix = size.charAt(size.length() - 1);
            long numbers = Long.valueOf(size.substring(0, size.length() - 1));
            switch (suffix) {
            case 'B':
                sizeInBytes = numbers;
                break;
            case 'K':
                sizeInBytes = numbers * (long) BenchmarkUtils.KiB_IN_BYTES;
                break;
            case 'M':
                sizeInBytes = numbers * (long) BenchmarkUtils.MiB_IN_BYTES;
                break;
            case 'G':
                sizeInBytes = numbers * (long) BenchmarkUtils.GiB_IN_BYTES;
                break;
            default:
                sizeInBytes = 0L;
                break;
            }
        }
        return sizeInBytes;
    }

    List<String> getArguments() {
        return arguments;
    }
}