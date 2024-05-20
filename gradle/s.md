# Bing Maps KML Viewer

This project is an Android Accessibility Service designed to automatically uninstall applications sequentially based on user selection, excluding system applications. It lists all apps installed on the phone (excluding system apps) and allows users to easily uninstall multiple apps without manual intervention. This service is primarily created as a sample project for Accessibility Service and aims to serve as a reference for using the Accessibility API as well as being a useful tool.

<img src="Screenshot_20240202_232625.png" width="300"> 

## Features

- **App Listing**: Lists all apps installed on the phone, excluding system applications.
- **User Selection**: Users can select the apps they wish to uninstall.
- **Automatic Uninstallation**: The selected apps are automatically uninstalled through the Accessibility Service, facilitating the batch uninstallation process.
- **Accessibility API Usage**: Interacts with system dialogs using the Accessibility API to confirm app uninstallations.

## How It Works

1. **App Listing**: The service lists all non-system applications installed on the phone.
2. **User Selection**: Users select the apps they want to uninstall from the list.
3. **Automatic Uninstallation**: The selected apps are automatically uninstalled in sequence via the Accessibility Service.

## Setup

To use the project:

1. **Enable Accessibility Service**: Before using the app uninstaller service, make sure to enable it in your device's accessibility settings.
2. **Initiate Uninstallation Process**: Select the apps you wish to delete from the user interface and start the uninstallation process.

## Contributing

Your contributions are welcome! If you have suggestions or improvements, please fork the repository and submit a pull request.

## License

This project is open-sourced under the MIT license to allow everyone to freely use the source code.

## Disclaimer

This project is developed for educational and demonstration purposes. Please use it responsibly and ensure compliance with Google's Accessibility Services policies and guidelines.
