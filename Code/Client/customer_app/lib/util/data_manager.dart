/// Singleton class to store useful data through the app
class DataManager {
  static final DataManager _instance = DataManager._();
  String token = "";
  String serverAddress = "";

  factory DataManager() => _instance;

  DataManager._();
}
