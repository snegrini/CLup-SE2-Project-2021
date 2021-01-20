import 'dart:convert';

import 'package:http/http.dart' as http;

/// Class that handles the API requests to the server.
class ApiManager {
  static final _baseUrl =
      "http://192.168.178.36:8080/CLupWeb_war_exploded/api/";
  static final _tokenUrl = "customer_token";
  static final _storeListUrl = "get_stores";
  static final _ticketListUrl = "get_tickets";
  static final _addTicketUrl = "add_ticket";
  static final _deleteTicketUrl = "delete_ticket";

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

  /// Performs the request to the server of a customer token. A [customerId] is
  /// sent in the request.
  static Future<String> customerTokenRequest(String customerId) async {
    var url = _baseUrl + _tokenUrl;
    var body = {
      'customer_id': customerId,
    };

    var jsonResponse = await _makeRequest(url, body);

    return jsonResponse['token'];
  }

  /// Performs the request to the server of the stores list. The [token] is mandatory
  /// while the [filter] is optional.
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

  /// Performs the request to the server of the tickets list of the customer.
  /// The [token] is mandatory.
  static Future<List<dynamic>> ticketListRequest(String token) async {
    var url = _baseUrl + _ticketListUrl;
    var body = {
      'token': token,
    };

    var jsonResponse = await _makeRequest(url, body);

    return jsonResponse['tickets'];
  }
}
