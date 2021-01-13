import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:store_app/util/clup_colors.dart';
import 'package:store_app/views/qr_button_page.dart';
import 'package:store_app/util/api_manager.dart';

class LoginPage extends StatefulWidget {
  _LoginState createState() => _LoginState();
}

class _LoginState extends State<LoginPage> {
  final _storage = new FlutterSecureStorage();
  final _formKey = GlobalKey<FormState>();

  final TextEditingController _userCodeController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  bool _loginButtonDisabled = false;
  bool _gotError = false;
  String _error = "";

  String _validateUserCode(String value) {
    if (value.isEmpty) {
      return 'Please enter some text';
    }

    return null;
  }

  String _validatePassword(String value) {
    if (value.isEmpty) {
      return 'Please enter some text';
    }

    return null;
  }

  Future<void> _submit() async {
    if (this._formKey.currentState.validate()) {
      _formKey.currentState.save();

      setState(() {
        _loginButtonDisabled = true;
      });

      ApiManager.loginRequest(
              _userCodeController.text, _passwordController.text)
          .then((value) {
        _storage.write(key: 'jwt', value: value);
        Navigator.pop(context);
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => QrButtonPage()),
        );
      }).catchError((e) {
        setState(() {
          _gotError = true;
          _error = e;
          _loginButtonDisabled = false;
        });
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text('CLup Store App'),
          backgroundColor: ClupColors.grapefruit,
        ),
        body: new Container(
            padding: new EdgeInsets.all(20.0),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  TextFormField(
                      controller: _userCodeController,
                      decoration: const InputDecoration(
                        hintText: 'User Code',
                      ),
                      validator: _validateUserCode),
                  TextFormField(
                    controller: _passwordController,
                    decoration: const InputDecoration(
                      hintText: 'Password',
                    ),
                    validator: _validatePassword,
                    enableSuggestions: false,
                    autocorrect: false,
                    obscureText: true,
                  ),
                  if (_gotError)
                    new Container(
                        margin: const EdgeInsets.only(top: 20.0),
                        child: Text(_error,
                            style: TextStyle(
                                color: ClupColors.grapefruit,
                                fontSize: 17,
                                fontWeight: FontWeight.bold))),
                  Container(
                      width: MediaQuery.of(context).size.width,
                      child: new RaisedButton(
                        child: new Text(
                          'Login',
                          style: new TextStyle(color: Colors.white),
                        ),
                        onPressed: _loginButtonDisabled ? null : _submit,
                        color: ClupColors.grapefruit,
                        disabledColor: ClupColors.disabledGrapefruit,
                      ),
                      margin: new EdgeInsets.only(top: 10.0)),
                ],
              ),
            )));
  }

  @override
  void initState() {
    super.initState();
    _storage.read(key: 'jwt').then((String value) {
      if (value != null) {
        Navigator.pop(context);
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => QrButtonPage()),
        );
      }
    });
  }
}
