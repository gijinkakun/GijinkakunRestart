# GijinkakunRestart Plugin

GijinkakunRestart is a Bukkit/Spigot plugin that automatically restarts the server at a configured time. Players receive advance warnings through chat messages with sound effects, and server admins can trigger immediate restarts using a command.

## Features

- Automatically restart the server at a configured time.
- Advance warnings with customizable messages and sound effects.
- Real-time countdown for the final 10 seconds before the restart.
- Command to force an immediate restart with advance warnings.
- Command auto-completion for ease of use.

## Commands

### `/gijinkakunrestart <30|15|10|5|1>`

Forces the server to run the restart sequence based on the specified time in minutes.

- **Usage:** `/gijinkakunrestart <30|15|10|5|1>`
- **Permission:** This command can only be run by players with `gijinkakunrestart.use` permission (typically ops).

## Permissions

- **gijinkakunrestart.use:** Allows the use of the `/gijinkakunrestart` command. By default, this permission is granted to server operators (op).

## Configuration

The plugin uses a configuration file (`config.yml`) to set the restart time and customize the warning messages.

### `config.yml`

restart-time: "08:30" # Time in 24hr format when the server will restart

messages:
  warning_30: "30 mins Until Restart"
  warning_15: "15 mins Until Restart"
  warning_10: "10 mins Until Restart"
  warning_5: "5 mins Until Restart"
  warning_1: "1 min Until Restart"
  countdown: "%seconds% seconds until restart."
  restart: "Server Restarting..."
  forced_restart: "Forced restart scheduled in %minutes% minutes."

## Installation

1. Download the plugin JAR file and place it in your server's plugins directory.
2. Start your server to load the plugin.
3. Configure the restart time and messages in the config.yml file as needed.

## Building the Plugin

If you want to build the plugin from source, follow these steps:

1. Clone the repository.
2. Ensure you have Maven installed.
3. Run mvn clean install to build the plugin.
4. The compiled JAR file will be located in the target directory.

## Usage

1. The server will automatically restart at the configured time with advance warnings.
2. Use the /gijinkakunrestart <30|15|10|5|1> command to force an immediate restart with advance warnings.
3. Players receive real-time updates on the restart progress via chat messages and sound effects.
4. The server restarts after the final 10-second countdown.

## Event Listeners

- **Custom Event Listeners:** Handles scheduling and broadcasting warnings and the restart countdown.

## Dependencies

- No external dependencies are required for this plugin.

## Troubleshooting

- Ensure that the plugin is correctly placed in the `plugins` directory and the server is restarted.
- Verify that the required commands are being typed correctly by players.
- Check the server console for any error messages related to the plugin.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## Contact

For any questions or support, feel free to open an issue on the GitHub repository.
