import 'dart:convert';

import 'package:http/http.dart' as http;

class ApiManager {
  static final _baseUrl =
      "http://192.168.178.36:8080/CLupWeb_war_exploded/api/";
  static final _loginUrl = "login";

  ApiManager._(); // Private Constructor

  static Future<String> loginRequest(String usercode, String password) async {
    var url = _baseUrl + _loginUrl;

    var response;
    try {
      response = await http.post(url, body: {
        'usercode': usercode,
        'password': password
      }).timeout(Duration(seconds: 2), onTimeout: () {
        throw 'Timeout';
      });
    } catch (_) {
      return Future.error('Couldn\'t reach the server');
    }

    if (response.statusCode != 200) {
      return Future.error('Couldn\'t reach the server');
    }

    Map<String, dynamic> jsonResponse = jsonDecode(response.body);

    // TODO Check invalid response
    if (jsonResponse['status'] == 'ERROR') {
      return Future.error(jsonResponse['message']);
    }

    return jsonResponse['token'];
  }
}
