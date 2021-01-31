import 'package:customer_app/model/store.dart';
import 'package:customer_app/util/api_manager.dart';
import 'package:customer_app/util/clup_colors.dart';
import 'package:customer_app/util/data_manager.dart';
import 'package:customer_app/views/passes_page.dart';
import 'package:customer_app/views/store_detail_page.dart';
import 'package:customer_app/views/stores_page.dart';
import 'package:flutter/material.dart';

/// Home page of the app with a bottom navigation bar to switch from the store list
/// to the store passes list and vice versa.
class HomePage extends StatefulWidget {
  @override
  _HomeState createState() => _HomeState();
}

class _HomeState extends State<HomePage> {
  int _selectedIndex = 0;
  List<Widget> _actions;

  static List<Widget> _widgetOptions = <Widget>[StoresPage(), PassesPage()];

  void _onItemTapped(int index) {
    List<Widget> actions = [];
    if (index == 0) {
      actions = [
        (IconButton(
            icon: Icon(Icons.search),
            onPressed: () {
              showSearch(context: context, delegate: StoreSearch());
            }))
      ];
    }

    setState(() {
      _selectedIndex = index;
      _actions = actions;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('CLup'),
        actions: _actions,
        backgroundColor: ClupColors.grapefruit,
      ),
      body: Center(
        child: _widgetOptions.elementAt(_selectedIndex),
      ),
      bottomNavigationBar: BottomNavigationBar(
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.list),
            label: 'List',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.confirmation_number),
            label: 'My Passes',
          ),
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: ClupColors.grapefruit,
        onTap: _onItemTapped,
      ),
    );
  }

  @override
  void initState() {
    super.initState();

    setState(() {
      _actions = [
        (IconButton(
            icon: Icon(Icons.search),
            onPressed: () {
              showSearch(context: context, delegate: StoreSearch());
            }))
      ];
    });
  }
}

class StoreSearch extends SearchDelegate {
  @override
  List<Widget> buildActions(BuildContext context) {
    return [
      IconButton(
          icon: Icon(Icons.clear),
          onPressed: () {
            query = "";
          })
    ];
  }

  @override
  Widget buildLeading(BuildContext context) {
    return IconButton(
        icon: AnimatedIcon(
          icon: AnimatedIcons.menu_arrow,
          progress: transitionAnimation,
        ),
        onPressed: () {
          close(context, null);
        });
  }

  @override
  Widget buildResults(BuildContext context) {
    return FutureBuilder<List<Store>>(
      future: _fetchStoreList(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          if (snapshot.error != null) {
            return Center(child: Text(snapshot.error),);
          } else if (snapshot.data.isEmpty) {
            return Center(child: Text("Wow, such empty"),);
          } else {
            return ListView.separated(
              itemBuilder: (context, index) {
                return _buildRow(context, snapshot.data[index]);
              },
              itemCount: snapshot.data.length,
              separatorBuilder: (context, index) => Divider(color: Colors.grey),
            );
          }
        } else {
          return Center(
            child: CircularProgressIndicator(
                valueColor:
                new AlwaysStoppedAnimation<Color>(ClupColors.grapefruit)),
          );
        }
      },
    );
  }

  @override
  Widget buildSuggestions(BuildContext context) {
    return Container();
  }

  Future<List<Store>> _fetchStoreList() async {
    String token = DataManager().token;
    var storeList = await ApiManager.storeListRequest(token, query);
    return storeList.map((e) => Store.fromJson(e)).toList();
  }

  Widget _buildRow(BuildContext context, Store store) {
    return new ListTile(
      title: new Text(store.name),
      subtitle: Text(store.address.toString()),
      trailing: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text('Wait Time'),
          SizedBox(height: 2),
          (store.estimateTime < 45)
              ? Text(
            store.estimateTime.toString() + ' min',
            style: TextStyle(color: Colors.green),
          )
              : Text(store.estimateTime.toString() + ' min',
              style: TextStyle(color: ClupColors.grapefruit))
        ],
      ),
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => StoreDetailPage(store.id)),
        );
      },
    );
  }
}