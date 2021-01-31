import 'package:customer_app/util/auth_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
import 'package:customer_app/views/home_page.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ServerAddressPage extends StatefulWidget {
  _ServerAddressState createState() => _ServerAddressState();
}

class _ServerAddressState extends State<ServerAddressPage> {
  final _addressTextController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  String _errorText = "";
  bool _disabled = false;
  bool _error = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        backgroundColor: Colors.white,
        body: Form(
            key: _formKey,
            child: Padding(
              padding: EdgeInsets.symmetric(vertical: 50.0, horizontal: 30.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Text(
                    "Set the server address",
                    style: TextStyle(fontSize: 20),
                  ),
                  Padding(
                    padding: EdgeInsets.symmetric(vertical: 15.0),
                    child: TextFormField(
                        controller: _addressTextController,
                        validator: _validateAddress,
                        decoration: InputDecoration(
                            hintText: 'Enter the server address')),
                  ),
                  RaisedButton(
                      onPressed: _disabled ? null : _submit,
                      color: ClupColors.grapefruit,
                      disabledColor: ClupColors.disabledGrapefruit,
                      child: Padding(
                          padding: EdgeInsets.symmetric(vertical: 10.0),
                          child: Text(
                            'Set server address',
                            style: TextStyle(color: Colors.white),
                          ))),
                  if (_error) Text(_errorText),
                ],
              ),
            )));
  }

  void _submit() {
    if (this._formKey.currentState.validate()) {
      _formKey.currentState.save();

      setState(() {
        _disabled = true;
        _error = false;
      });
      DataManager().serverAddress = _addressTextController.text;
      _requestCustomerToken();
    }
  }

  /// Requests a customer token to the server
  Future<void> _requestCustomerToken() async {
    AuthManager authManager = AuthManager();
    String token;
    try {
      token = await authManager.requestCustomerToken();
      authManager.writeAuthToken(token);
      DataManager().token = token;

      SharedPreferences prefs = await SharedPreferences.getInstance();
      prefs.setString('serverAddress', DataManager().serverAddress);

      _redirectToHome();
    } catch (err) {
      setState(() {
        _disabled = false;
        _error = true;
        _errorText = err;
      });
    }
  }

  void _redirectToHome() {
    Navigator.pop(context);
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => HomePage()),
    );
  }

  String _validateAddress(String value) {
    if (value.isEmpty) {
      return 'Please enter some text';
    }

    RegExp urlRegex = RegExp(r'^(http:\/\/|https:\/\/).+\/$');
    if (!urlRegex.hasMatch(value)) {
      return 'Please include http(s):// and the final /';
    }

    return null;
  }
}
