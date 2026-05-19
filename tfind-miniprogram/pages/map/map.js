var app = getApp();

Page({
  data: {
    latitude: 39.908823,
    longitude: 116.397470,
    markers: [],
    toilets: [],
    scale: 15,
    showList: false,
    selectedToilet: null
  },

  onLoad: function () {
    this.initLocation();
  },

  onShow: function () {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 });
    }
  },

  initLocation: function () {
    var that = this;
    if (app.globalData.location) {
      that.setData({
        latitude: app.globalData.location.latitude,
        longitude: app.globalData.location.longitude
      });
      that.loadNearbyToilets();
      return;
    }
    wx.getLocation({
      type: 'gcj02',
      success: function (res) {
        app.globalData.location = {
          latitude: res.latitude,
          longitude: res.longitude
        };
        that.setData({
          latitude: res.latitude,
          longitude: res.longitude
        });
        that.loadNearbyToilets();
      },
      fail: function () {
        wx.showModal({
          title: '位置授权',
          content: '需要获取您的位置信息以显示附近厕所',
          confirmText: '去授权',
          success: function (modalRes) {
            if (modalRes.confirm) {
              wx.openSetting();
            }
          }
        });
      }
    });
  },

  loadNearbyToilets: function () {
    var that = this;
    if (!app.globalData.location) return;
    app.request({
      url: '/api/toilet/nearby',
      method: 'GET',
      data: {
        lat: app.globalData.location.latitude,
        lng: app.globalData.location.longitude,
        mode: 'speed',
        distance: 5.0
      },
      success: function (res) {
        if (res.data && res.data.code === 200) {
          var toilets = res.data.data || [];
          var markers = toilets.map(function (t, index) {
            return {
              id: index,
              latitude: t.latitude,
              longitude: t.longitude,
              title: t.name,
              iconPath: '/images/marker.png',
              width: 32,
              height: 40,
              callout: {
                content: t.name,
                color: '#333333',
                fontSize: 12,
                borderRadius: 8,
                bgColor: '#ffffff',
                padding: 6,
                display: 'BYCLICK'
              }
            };
          });
          that.setData({
            toilets: toilets,
            markers: markers
          });
        }
      }
    });
  },

  onMarkerTap: function (e) {
    var index = e.markerId;
    var toilet = this.data.toilets[index];
    if (toilet) {
      this.setData({
        selectedToilet: toilet,
        showList: true
      });
    }
  },

  onRegionChange: function (e) {
    if (e.type === 'end') {
      this.setData({
        latitude: e.detail.latitude,
        longitude: e.detail.longitude
      });
    }
  },

  goToDetail: function (e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/toilet-detail/toilet-detail?id=' + id });
  },

  closeList: function () {
    this.setData({ showList: false });
  },

  refreshMap: function () {
    this.loadNearbyToilets();
  }
});
