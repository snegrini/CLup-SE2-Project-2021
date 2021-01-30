import 'dart:convert';

import 'package:customer_app/model/store.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
import 'package:customer_app/views/store_detail_page.dart';
import 'package:flutter/material.dart';


/// Page that displays the list of stores
class StoresPage extends StatefulWidget {
  @override
  _StoresState createState() => _StoresState();
}

class _StoresState extends State<StoresPage> {
  List<Store> _list;
  bool _loaded = false;
  bool _gotError = false;
  String _text;

  /// Displays a progress bar if the page is loading, a text if the fetch got an
  /// error and the list if everything gone fine. If the list is empty a message
  /// is shown.
  @override
  Widget build(BuildContext context) {
    if (!_gotError) {
      if (_loaded) {
        if (_list.isEmpty) {
          return Center(child: Text('No stores here!'));
        } else {
          return ListView.separated(
            itemCount: _list.length,
            itemBuilder: (context, index) {
              return _buildRow(_list[index]);
            },
            separatorBuilder: (context, index) => Divider(color: Colors.grey),
          );
        }
      } else {
        return CircularProgressIndicator(
            valueColor:
                new AlwaysStoppedAnimation<Color>(ClupColors.grapefruit));
      }
    } else {
      return Center(child: Text(_text));
    }
  }

  Widget _buildRow(Store store) {
    return new ListTile(
      title: new Text(store.name),
      subtitle: Text(store.address.toString()),
        leading: SizedBox(
            height: 100.0,
            width: 100.0,
            child: Image.memory(base64Decode(store.image))
        ),
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => StoreDetailPage(store.id)),
        );
      },
    );
  }

  @override
  void initState() {
    super.initState();
    _fetchStoreList();
  }

  /// Fetches the list of stores from the server. A [filter] can be passed to request
  /// a filtered list.
  Future<void> _fetchStoreList([String filter]) async {
    try {
      String token = DataManager().token;

      var storeList = await ApiManager.storeListRequest(token, filter);

      setState(() {
        _loaded = true;
        _list = storeList.map((e) => Store.fromJson(e)).toList();
      });
    } catch (err) {
      setState(() {
        _gotError = true;
        _text = err;
        _loaded = true;
      });
    }
  }
}
