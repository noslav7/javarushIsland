package javarush.island;


import org.apache.commons.cli.*;
//Using Apache CommonCLI library we initialize parser of console arguments
public class MainClass {
        public static void main(String[] args) {

        Options options = new Options();

        Option settingsFileOption = new Option("sf", "settings_file", true, "settings file path");

        settingsFileOption.setRequired(false);

        options.addOption(settingsFileOption);

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser lineParser = new DefaultParser();
        CommandLine commandLine = null;

        try {
            commandLine = lineParser.parse(options, args);
        }catch (ParseException exception) {
            formatter.printHelp("settings_file", options);

            System.exit(1);
        }

        String settingsFilePath = commandLine.getOptionValue(settingsFileOption);

        if(settingsFilePath == null) {
            System.out.println("Starting simulation with default settings...");

            System.out.println("You can modify this with command argument: ");
            formatter.printHelp("settings_file", options);
        }

        Island.startSimulation(settingsFilePath);
    }
}
