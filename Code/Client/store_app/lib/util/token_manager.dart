/// Singleton class to store the token through the app
class TokenManager {
  static final TokenManager _instance = TokenManager._internal();
  String token;

  factory TokenManager() => _instance;

  TokenManager._internal();
}
