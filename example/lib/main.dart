import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_cyber_vision/flutter_cyber_vision.dart';
import 'package:image_picker/image_picker.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  var _img, _imgPath, image;
  Widget _faceMeshView = Text("启动相机");
  Widget _animeView = Text("启动3d");
  var _showFaceFlag = false;

  static const MethodChannel _channel =
      const MethodChannel('flutter_cyber_vision');

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterCyberVision.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future getImage() async {
    // final picker = ImagePicker();
    image = await ImagePicker.pickImage(source: ImageSource.gallery);
    setState(() {
      _img = image;
    });
  }

  /*图片控件*/
  Widget _ImageView(imgPath) {
    if (imgPath == null) {
      return Center(
        child: Text("请选择图片或拍照"),
      );
    } else {
      return Image.file(
        imgPath,
      );
    }
  }

  Widget _FaceMeshView(showFaceFlag) {
    if (showFaceFlag) {
      //false显示
      _channel.invokeMethod('getFaceMeshView');
      return AndroidView(viewType: "faceMeshView");
    } else {
      return Center(
        child: Text("启动相机"),
      );
    }
  }

  Future getFaceMesh() async {
    Widget aView;
    if (_showFaceFlag == false) {
      await _channel.invokeMethod("getFaceMeshView");
      aView = AndroidView(viewType: "faceMeshView");
    } else
      aView = Text("启动相机");

    setState(() {
      _showFaceFlag = !_showFaceFlag;
      _faceMeshView = aView;
    });
  }

  //获取method的时候一定要加Future,不然异步获取不到
 Future getAnime() async{
    var flag = "test";
    Widget aaView;
    var path;
    if (flag=="test")
      path = "sda";
    else path = image.path;

    Map<String, Object> map = {"imgPath": path};
    await _channel.invokeMethod("getAnimeView", map);
    aaView = AndroidView(viewType: "animeView");
    setState(() {
      _imgPath = image.path;
      _animeView = aaView;
    });
  }

  Future getAnime_test() async{
    Map<String, Object> map = {"imgPath": image.path};
    await _channel.invokeMethod("getAnime", map);
  }


  @override
  Widget build(BuildContext context) {
    double _imgSize = 256;
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: SingleChildScrollView(
          child: Column(
            children: <Widget>[
              Container(
                width: _imgSize,
                height: _imgSize,
                decoration: BoxDecoration(color: Colors.blue),
                child: Center(child: _faceMeshView),
              ),
              Container(
                width: _imgSize,
                height: _imgSize,
                decoration: BoxDecoration(color: Colors.cyan),
                child: _animeView,//Center(child: _ImageView(_img)),
              ),
              Text("图片路径：$_imgPath"),
              Center(
                child: Row(
                  children: [
                    /*相册照片*/
                    RaisedButton(
                      onPressed: getImage,
                      textColor: Colors.blue,
                      child: Text("load"),
                    ),
                    /*人脸关键点检测*/
                    Offstage(
                        offstage: _showFaceFlag,
                        child: RaisedButton(
                          onPressed: getFaceMesh,
                          child: Text("face"),
                        )),
                    Offstage(
                        offstage: !_showFaceFlag,
                        child: RaisedButton(
                          onPressed: getFaceMesh,
                          child: Text("close"),
                        )),
                    /*3d动漫头像*/
                    RaisedButton(
                      onPressed: getAnime,
                      child: Text("anime"),
                    ),
                    RaisedButton(
                      onPressed: getAnime_test,
                      child: Text("test"),
                    )
                  ],
                ),
              ),
              // Text("图片路径：" + _imgPath.toString())
            ],
          ),
        ),
      ),
    );
  }
}
