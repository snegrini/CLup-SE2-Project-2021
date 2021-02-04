import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:store_app/util/clup_colors.dart';
import 'package:store_app/util/data_manager.dart';
import 'package:store_app/views/login_page.dart';
import 'package:store_app/views/qr_button_page.dart';
import 'package:store_app/views/server_address_page.dart';

void main() => runApp(ClupApp());

class ClupApp extends StatefulWidget {
  _ClupAppState createState() => _ClupAppState();
}

class _ClupAppState extends State<ClupApp> {
  static const String _title = 'CLup Store App';
  Widget _homePage;

  @override
  Widget build(BuildContext context) {
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.portraitUp,
      DeviceOrientation.portraitDown,
    ]);

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
    FlutterSecureStorage storage = new FlutterSecureStorage();

    // Checks the key presence in the shared preferences, if not set redirect to the server set page.
    if (prefs.containsKey('serverAddress')) {
      DataManager().serverAddress = (prefs.getString('serverAddress'));

      // If token is set in the secure storage redirects to the homepage. Otherwise a customer request to server is performed.
      if ((await storage.read(key: 'jwt')) != null) {
        DataManager().token = await storage.read(key: 'jwt');

        setState(() {
          _homePage = QrButtonPage();
        });
      } else {
        setState(() {
          _homePage = LoginPage();
        });
      }
    } else {
      setState(() {
        _homePage = ServerAddressPage();
      });
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
}
