import 'package:customer_app/model/store.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
import 'package:flutter/material.dart';

class StoreDetailPage extends StatefulWidget {
  final int storeId;

  const StoreDetailPage(this.storeId);

  _StoreDetailState createState() => _StoreDetailState();
}

class _StoreDetailState extends State<StoreDetailPage> {
  Widget _body;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Store'),
        backgroundColor: ClupColors.grapefruit,
      ),
      body: _body,
    );
  }

  @override
  void initState() {
    super.initState();
    _setLoadingBar();
    _storeDetailRequest();
  }

  Future<void> _storeDetailRequest() async {
    try {
      var storeJson = await ApiManager.storeDetailRequest(
          DataManager().token, widget.storeId);

      Store store = Store.fromJson(storeJson);
      _setStoreDetails(store);
    } catch (e) {
      setState(() {
        _setErrorPage(e);
      });
    }
  }

  /// Sets a page for displaying a loading bar.
  void _setLoadingBar() {
    setState(() {
      _body = Center(
          child: CircularProgressIndicator(
              valueColor:
                  new AlwaysStoppedAnimation<Color>(ClupColors.grapefruit)));
    });
  }

  void _setErrorPage(String error) {
    setState(() {
      _body = Center(child: Text(error));
    });
  }

  void _setStoreDetails(Store store) {
    setState(() {
      _body = Center(child: Text(store.name));
    });
  }
}
