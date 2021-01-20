import 'dart:convert';

import 'package:http/http.dart' as http;

/// Class that handles the API requests to the server.
class ApiManager {
  static final _baseUrl =
      "http://192.168.1.100:8080/CLupWeb_war_exploded/api/";
  static final _loginUrl = "login";
  static final _validateUrl = "validate_ticket";

  ApiManager._(); // Private Constructor

  /// Makes a generic request to the server and handles all the errors.
  static Future<Map<String, dynamic>> _makeRequest(url, body) async {
    // Performs an API request
    var response;
    try {
      response = await http.post(url, body: body).timeout(Duration(seconds: 2),
          onTimeout: () {
            throw 'Timeout';
          });
    } catch (_) {
      return Future.error('Couldn\'t reach the server');
    }

    if (response.statusCode != 200) {
      return Future.error('Couldn\'t reach the server');
    }

    Map<String, dynamic> jsonResponse;
    try {
      jsonResponse = jsonDecode(response.body);
    } catch (_) {
      return Future.error('Invalid response format');
    }

    if (jsonResponse['status'] == 'ERROR') {
      return Future.error(jsonResponse['message']);
    }

    return jsonResponse;
  }

  /// Performs a login request with the credentials [usercode] and [password].
  static Future<String> loginRequest(String usercode, String password) async {
    var url = _baseUrl + _loginUrl;

    var body = {
      'usercode': usercode,
      'password': password
    };

    var jsonResponse = await _makeRequest(url, body);

    return jsonResponse['token'];
  }

  /// Performs a store pass validation request passing the auth [token] and the [passcode].
  static Future<String> validateRequest(String token, String passcode) async {
    var url = _baseUrl + _validateUrl;

    var body = {
      'token': token,
      'passcode': passcode,
    };

    var jsonResponse = await _makeRequest(url, body);

    return jsonResponse['message'];
  }
}
