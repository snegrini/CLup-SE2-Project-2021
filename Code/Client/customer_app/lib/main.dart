import 'package:customer_app/util/auth_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
import 'package:customer_app/views/home_page.dart';
import 'package:customer_app/views/server_address_page.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() => runApp(ClupApp());

class ClupApp extends StatefulWidget {
  _ClupAppState createState() => _ClupAppState();
}

class _ClupAppState extends State<ClupApp> {
  static const String _title = 'CLup App';
  Widget _homePage;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: _title,
      home: _homePage,
    );
  }

  @override
  void initState() {
    super.initState();
    _setLoadingBar();
    _initProperties();
  }

  /// Initializes the auth token and server address
  Future<void> _initProperties() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    AuthManager authManager = AuthManager();

    // Checks the key presence in the shared preferences, if not set redirect to the server set page.
    if (prefs.containsKey('serverAddress')) {
      DataManager().serverAddress = (prefs.getString('serverAddress'));

      // If token is set in the secure storage redirects to the homepage. Otherwise a customer request to server is performed.
      if (await authManager.isAuthTokenSet()) {
        DataManager().token = await authManager.getAuthToken();

        setState(() {
          _homePage = HomePage();
        });
      } else {
        _requestCustomerToken(authManager);
      }
    } else {
      setState(() {
        _homePage = ServerAddressPage();
      });
    }
  }

  /// Requests a customer token to the server
  void _requestCustomerToken(AuthManager authManager) async {
    String token;
    try {
      token = await authManager.requestCustomerToken();
      authManager.writeAuthToken(token);
      DataManager().token = token;

      setState(() {
        _homePage = HomePage();
      });
    } catch (err) {
      _setErrorPage(err);
    }
  }

  /// Sets a page for displaying a loading bar.
  void _setLoadingBar() {
    setState(() {
      _homePage = new Scaffold(
          backgroundColor: Colors.white,
          body: Center(
              child: CircularProgressIndicator(
                  valueColor: new AlwaysStoppedAnimation<Color>(
                      ClupColors.grapefruit))));
    });
  }

  /// Sets a page for displaying an error.
  void _setErrorPage(String text) {
    setState(() {
      _homePage = new Scaffold(
        backgroundColor: Colors.white,
        body: Center(child: Text(text)),
      );
    });
  }
}
