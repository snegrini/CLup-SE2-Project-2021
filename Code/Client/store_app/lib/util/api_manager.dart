import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:store_app/util/data_manager.dart';

/// Class that handles the API requests to the server.
class ApiManager {
  static final _apiPath = "api/";
  static final _pingUrl = "ping";
  static final _loginUrl = "login";
  static final _validateUrl = "validate_ticket";

  ApiManager._(); // Private Constructor

  /// Makes a generic request to the server and handles all the errors.
  static Future<Map<String, dynamic>> _makeRequest(url, body) async {
    // Performs an API request
    var response;
    url = DataManager().serverAddress + _apiPath + url;

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
  static Future<void> pingRequest() async {
    var body = {};

    var jsonResponse;
    try {
      jsonResponse = await _makeRequest(_pingUrl, body);
    } catch (e) {
      return Future.error(e);
    }

    if (jsonResponse == null || jsonResponse['status'] != 'OK') {
      return Future.error('Couldn\'t reach the server');
    }
  }

  /// Performs a login request with the credentials [usercode] and [password].
  static Future<String> loginRequest(String usercode, String password) async {
    var body = {'usercode': usercode, 'password': password};

    var jsonResponse;
    try {
      jsonResponse = await _makeRequest(_loginUrl, body);
    } catch (e) {
      return Future.error(e);
    }

    return jsonResponse['token'];
  }

  /// Performs a store pass validation request passing the auth [token] and the [passcode].
  static Future<String> validateRequest(String token, String passcode) async {
    var body = {
      'token': token,
      'passcode': passcode,
    };

    var jsonResponse;
    try {
      jsonResponse = await _makeRequest(_validateUrl, body);
    } catch (e) {
      return Future.error(e);
    }

    return jsonResponse['message'];
  }
}
