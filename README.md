# rxbus

RxBus工具类。

1、该项目基于[RxJava2](https://github.com/ReactiveX/RxJava) & [RxAndroid](https://github.com/ReactiveX/RxAndroid)。

2、通过`@RxBusSubscribe`注解方法来接收消息，其中可以设置标签组、线程、Sticky标记。

3、可以发送普通消息和Sticky消息。

4、自动防重复注册宿主、自动防重复注册标签（及同一个宿主下的标签不重复，如果重复了，只有第一次有效。）。

5、支持背压，采用的策略是BackpressureStrategy.DROP。

## 使用方法：

1、引用

在Project的gradle中加入：
```groovy
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```
在Module的gradle中加入：
```groovy
    dependencies {
        implementation 'com.github.like5188:RxBus:1.3.0'
        kapt 'com.github.like5188.RxBus:rxbus-compiler:1.3.0'
    }
```

2、是否打印日志。注意：设置这个标记，会影响所有用了Logger库的库。
```java
    Logger.setDebugMode(true);
    Logger.setDebugMode(false);
```

3、在创建某个类的实例时调用`register(this)`方法进行注册宿主（通常在Activity的onCreate()方法中调用）。当在父类调用`register(this)`方法后，在子类无需再调用了，调用了也行，会自动防重复注册宿主。
```java
    RxBus.register(this);
```

4、在销毁某个类的实例时调用`unregister(this)`方法进行取消注册宿主（通常在Activity的onDestroy()方法中调用）。
```java
    RxBus.unregister(this);
```

5、发送普通消息可以使用`post()、postByTag()`方法。
```java
    RxBus.post();
    RxBus.post(object);
    RxBus.post("tag", object);
    RxBus.postByTag("tag");
```

6、发送Sticky消息使用`postSticky()`方法，注意Sticky消息在第一次接收后，就会销毁，以后就和普通消息一样了。和发送普通消息相比，发送Sticky消息，实际上就是延迟了第一次接收消息的时间。
```java
    RxBus.postSticky(object);
    RxBus.postSticky("tag", object);
```

7、接收消息和发送消息是一一对应的。使用`@RxBusSubscribe`注解一个方法，被注解的方法的参数最多只能是1个。只能被public修饰，且不能被static修饰(即被public void修饰)。其中可以设置标签组、线程(`RxBusThread`)、Sticky标记。
```java
    默认标签，无参
    
    发送消息：
    RxBus.post();
    
    接收消息：
    @RxBusSubscribe()
    public void test() {
    }
```
```java
    默认标签，有参
    
    发送消息：
    RxBus.post(123);
    
    接收消息：
    @RxBusSubscribe()
    public void test(int data) {
    }
```
```java
    自定义标签，无参
    
    发送消息：
    RxBus.postByTag("tag");
    
    接收消息：
    @RxBusSubscribe("tag")
    public void test() {
    }
```
```java
    自定义标签，有参
    
    发送消息：
    RxBus.post("tag", 123);
    
    接收消息：
    @RxBusSubscribe("tag")
    public void test(int data) {
    }
```
```java
    自定义标签数组，有参
    
    发送消息：
    RxBus.post("tag1", "1");
    RxBus.post("tag2", "2");
    
    接收消息：
    @RxBusSubscribe(value = {"tag1", "tag2"})
    public void test(String data) {
    }
```
```java
    自定义标签，有参
    
    发送Sticky消息：
    RxBus.postSticky("tag", "1");
    
    接收Sticky消息：
    @RxBusSubscribe(value = "tag", isSticky = true, thread = RxBusThread.IO)
    public void test(String data) {
    }
```
```java
    默认标签，有参
    
    发送Sticky消息：
    RxBus.postSticky("1");
    
    接收Sticky消息：
    @RxBusSubscribe(isSticky = true)
    public void test(String data) {
    }
```
8、引用的库
```java
    compile 'com.squareup:javapoet:1.8.0'// 自动生成源码的库
    compile 'com.google.auto.service:auto-service:1.0-rc3'// 自动生成注解处理器的库
    compile 'io.reactivex.rxjava2:rxjava:2.0.8'
    compile 'io.reactivex.rxjava2:rxandroid:2.0.1'// 自动生成的代码中需要
    compile project(':rxbus-annotations')// 注解中需要
```

# License
```xml
    Copyright 2017 like5188
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
