import 'package:customer_app/model/store.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:loading/indicator/ball_spin_fade_loader_indicator.dart';
import 'package:loading/loading.dart';

class ListPage extends StatefulWidget {
  @override
  _ListState createState() => _ListState();
}

class _ListState extends State<ListPage> {
  List<Store> _list;
  bool _loaded = false;
  bool _gotError = false;
  String _text;

  @override
  Widget build(BuildContext context) {
    if (!_gotError) {
      if (_loaded) {
        return ListView.separated(
          itemCount: _list.length,
          itemBuilder: (context, index) {
            return _buildRow(_list[index]);
          },
          separatorBuilder: (context, index) => Divider(color: Colors.grey),
        );
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
      var storage = new FlutterSecureStorage();
      String token = await storage.read(key: 'jwt');

      var storeList = await ApiManager.storeListRequest(token);

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
