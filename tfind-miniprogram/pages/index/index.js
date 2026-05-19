var app = getApp();

Page({
  data: {
    toilets: [],
    loading: false,
    hasResult: false,
    searched: false,
    mode: 'smart',
    distance: 6.0,
    locationReady: false
  },

  onLoad: function () {
    this.checkLocation();
  },

  onShow: function () {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 });
    }
  },

  checkLocation: function () {
    var that = this;
    if (app.globalData.location) {
      that.setData({ locationReady: true });
      return;
    }
    this.refreshLocation();
  },

  refreshLocation: function () {
    var that = this;
    wx.getLocation({
      type: 'gcj02',
      success: function (res) {
        app.globalData.location = {
          latitude: res.latitude,
          longitude: res.longitude
        };
        that.setData({ locationReady: true });
        wx.showToast({ title: '定位成功', icon: 'success', duration: 1500 });
      },
      fail: function () {
        that.setData({ locationReady: false });
        wx.showModal({
          title: '位置授权',
          content: '需要获取您的位置信息以提供附近厕所推荐',
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

  onModeChange: function (e) {
    this.setData({ mode: e.detail.value });
    app.globalData.recommendMode = e.detail.value;
  },

  onDistanceChange: function (e) {
    this.setData({ distance: parseFloat(e.detail.value) });
  },

  startFind: function () {
    var that = this;
    if (!app.globalData.location) {
      wx.showToast({ title: '请先授权位置信息', icon: 'none' });
      that.checkLocation();
      return;
    }
    that.setData({ loading: true, searched: true });
    app.request({
      url: '/api/toilet/nearby',
      method: 'GET',
      data: {
        lat: app.globalData.location.latitude,
        lng: app.globalData.location.longitude,
        mode: that.data.mode,
        distance: that.data.distance
      },
      success: function (res) {
        if (res.data && res.data.code === 200) {
          var toilets = res.data.data || [];
          toilets.forEach(function (t) {
            if (app.globalData.location) {
              t.distance = app.calculateDistance(
                app.globalData.location.latitude,
                app.globalData.location.longitude,
                t.latitude, t.longitude
              );
              if (t.distance < 1) {
                t.distanceText = (t.distance * 1000).toFixed(0) + 'm';
              } else {
                t.distanceText = t.distance.toFixed(1) + 'km';
              }
            }
          });
          that.setData({
            toilets: toilets,
            hasResult: toilets.length > 0,
            loading: false
          });
        } else {
          that.setData({ loading: false, hasResult: false });
          console.error('查询失败:', res.data);
          app.showError('查询失败');
        }
      },
      fail: function (err) {
        that.setData({ loading: false, hasResult: false });
        app.showError('网络错误: ' + (err.errMsg || '未知错误'));
      }
    });
  },

  goToDetail: function (e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/toilet-detail/toilet-detail?id=' + id });
  },

  goToRecommend: function () {
    wx.navigateTo({ url: '/pages/recommend/recommend' });
  }
});
