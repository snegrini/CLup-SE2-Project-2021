import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:customer_app/util/data_manager.dart';

/// Class that handles the API requests to the server.
class ApiManager {
  static final _apiPath = "api/";
  static final _tokenUrl = "customer_token";
  static final _storeListUrl = "get_stores";
  static final _ticketListUrl = "get_tickets";
  static final _storeDetail = "store_detail";
  static final _ticketDetail = "ticket_detail";
  static final _addTicketUrl = "add_ticket";
  static final _deleteTicketUrl = "delete_ticket";

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

  /// Performs the request to the server of a customer token. A [customerId] is
  /// sent in the request.
  static Future<String> customerTokenRequest(String customerId) async {
    var body = {
      'customer_id': customerId,
    };

    var jsonResponse;
    try {
      jsonResponse = await _makeRequest(_tokenUrl, body);
    } catch (e) {
      return Future.error(e);
    }

    return jsonResponse['token'];
  }

  /// Performs the request to the server of the stores list. The [token] is mandatory
  /// while the [filter] is optional.
  static Future<List<dynamic>> storeListRequest(String token,
      [String filter]) async {
    var body = {
      'token': token,
    };

    if (filter != null) {
      body['filter'] = filter;
    }

    var jsonResponse;
    try {
      jsonResponse = await _makeRequest(_storeListUrl, body);
    } catch (e) {
      return Future.error(e);
    }

    return jsonResponse['stores'];
  }

  /// Performs the request to the server of the tickets list of the customer.
  /// The [token] is mandatory.
  static Future<List<dynamic>> ticketListRequest(String token) async {
    var body = {
      'token': token,
    };

    var jsonResponse;
    try {
      jsonResponse = await _makeRequest(_ticketListUrl, body);
    } catch (e) {
      return Future.error(e);
    }

    return jsonResponse['tickets'];
  }

  static Future<dynamic> storeDetailRequest(String token, int storeId) async {
    var body = {
      'token': token,
      'store_id': storeId.toString(),
    };

    var jsonResponse;
    try {
      jsonResponse = await _makeRequest(_storeDetail, body);
    } catch (e) {
      return Future.error(e);
    }

    return jsonResponse['store'];
  }

  static Future<dynamic> ticketDetailRequest(String token, int ticketId) async {
    var body = {
      'token': token,
      'ticket_id': ticketId.toString(),
    };

    var jsonResponse;
    try {
      jsonResponse = await _makeRequest(_ticketDetail, body);
    } catch (e) {
      return Future.error(e);
    }

    return jsonResponse['ticket'];
  }

  static Future<dynamic> addTicketRequest(String token, int storeId) async {
    var body = {
      'token': token,
      'store_id': storeId.toString(),
    };

    var jsonResponse;
    try {
      jsonResponse = await _makeRequest(_addTicketUrl, body);
    } catch (e) {
      return Future.error(e);
    }

    return jsonResponse['ticket'];
  }

  static Future<dynamic> deleteTicketRequest(String token, int ticketId) async {
    var body = {
      'token': token,
      'ticket_id': ticketId.toString(),
    };

    var jsonResponse;
    try {
      jsonResponse = await _makeRequest(_deleteTicketUrl, body);
    } catch (e) {
      return Future.error(e);
    }

    return jsonResponse;
  }
}
