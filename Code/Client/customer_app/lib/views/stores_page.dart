import 'package:customer_app/model/store.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/token_manager.dart';
import 'package:flutter/material.dart';

class StoresPage extends StatefulWidget {
  @override
  _StoresState createState() => _StoresState();
}

class _StoresState extends State<StoresPage> {
  List<Store> _list;
  bool _loaded = false;
  bool _gotError = false;
  String _text;

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
      onTap: () {
        showDialog(
            context: context,
            builder: (context) => AlertDialog(
                  content: Text(store.name),
                ));
      },
    );
  }

  @override
  void initState() {
    super.initState();

    setState(() {
      _loaded = false;
    });

    _fetchStoreList();
  }

  Future<void> _fetchStoreList([String filter]) async {
    try {
      String token = TokenManager().token;

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
