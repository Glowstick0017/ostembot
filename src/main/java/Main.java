import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends ListenerAdapter {
    private static JDA jda;

    public static void main(String[] args) throws LoginException {
        String token = "TOKEN HERE";
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES);
        builder.setToken(token);
        builder.setRawEventsEnabled(true);
        builder.addEventListeners(new Main());
        builder.setActivity(Activity.watching("board members suffer"));
        jda = builder.build();

        //jda.upsertCommand("add", "add hours worked")
        //        .addOption(OptionType.STRING, "hours", "Enter hours worked", true)
        //        .addOption(OptionType.STRING, "tasks", "Enter what tasks you've done in this time", true).queue();
        //jda.upsertCommand("report", "Return an CSV file of all hours worked and tasks done").queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
        if (event.getName().equals("add")) {
            event.deferReply().queue();
            try {
                List<String[]> input = new ArrayList<>();
                input.add(new String[]
                    {DateTimeFormatter.ofPattern("yyyy/MM/dd").format(LocalDateTime.now()),
                            Objects.requireNonNull(event.getMember()).getEffectiveName(),
                            Objects.requireNonNull(event.getOption("hours")).getAsString() + "",
                            Objects.requireNonNull(event.getOption("tasks")).getAsString()
                    });
                addHoursTasksToCSV(input);
                EmbedBuilder msg = new EmbedBuilder();
                msg.setColor(Color.WHITE);
                msg.setTitle("Added hours to report");
                msg.setDescription(
                        DateTimeFormatter.ofPattern("yyyy/MM/dd").format(LocalDateTime.now()) + " : " +
                        event.getMember().getEffectiveName() + " : " +
                        Objects.requireNonNull(event.getOption("hours")).getAsString() + " : " +
                        Objects.requireNonNull(event.getOption("tasks")).getAsString()
                );
                event.getHook().sendMessageEmbeds(msg.build()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("Error Occured: " + e.getMessage()).queue();
            }
        }
        if (event.getName().equals("report")) {
            event.deferReply().queue();
            FileUpload fu = FileUpload.fromData(new File("report.csv"));
            event.getHook().sendMessage("Here is the most recent report:").addFiles(fu).queue();
        }
    }

    private void addHoursTasksToCSV(List<String[]> input) throws IOException {
        File csvFile = new File("report.csv");
        FileWriter fileWriter = new FileWriter(csvFile, true);
        for (String[] data : input) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                line.append(data[i]);
                if (i != data.length - 1) {
                    line.append(';');
                }
            }
            line.append("\n");
            fileWriter.write(line.toString());
        }
        fileWriter.close();
    }
}
