/// Singleton class to store the token through the app
class DataManager {
  static final DataManager _instance = DataManager._internal();
  String token = "";
  String serverAddress = "";

  factory DataManager() => _instance;

  DataManager._internal();
}
