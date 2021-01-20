import 'dart:convert';

import 'package:http/http.dart' as http;

class ApiManager {
  static final _baseUrl =
      "http://192.168.178.36:8080/CLupWeb_war_exploded/api/";
  static final _tokenUrl = "customer_token";
  static final _storeListUrl = "get_stores";
  static final _ticketListUrl = "get_tickets";
  static final _addTicketUrl = "add_ticket";
  static final _deleteTicketUrl = "delete_ticket";

  ApiManager._(); // Private Constructor

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

  static Future<String> customerTokenRequest(String customerId) async {
    var url = _baseUrl + _tokenUrl;
    var jsonResponse = await _makeRequest(url, {
      'customer_id': customerId,
    });

    return jsonResponse['token'];
  }

  static Future<List<dynamic>> storeListRequest(String token,
      [String filter]) async {
    var url = _baseUrl + _storeListUrl;
    var body = {
      'token': token,
    };

    if (filter != null) {
      body['filter'] = filter;
    }

    var jsonResponse = await _makeRequest(url, body);

    return jsonResponse['stores'];
  }
}
