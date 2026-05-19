var app = getApp();

Page({
  data: {
    toilets: [],
    loading: false,
    mode: 'smart',
    hasResult: false
  },

  onLoad: function () {
    this.setData({ mode: app.globalData.recommendMode || 'smart' });
    this.loadRecommend();
  },

  onModeChange: function (e) {
    this.setData({ mode: e.detail.value });
    app.globalData.recommendMode = e.detail.value;
    this.loadRecommend();
  },

  loadRecommend: function () {
    var that = this;
    if (!app.globalData.location) {
      wx.getLocation({
        type: 'gcj02',
        success: function (res) {
          app.globalData.location = {
            latitude: res.latitude,
            longitude: res.longitude
          };
          that.fetchRecommend();
        },
        fail: function () {
          wx.showToast({ title: '请授权位置信息', icon: 'none' });
        }
      });
      return;
    }
    that.fetchRecommend();
  },

  fetchRecommend: function () {
    var that = this;
    that.setData({ loading: true });
    app.request({
      url: '/api/toilet/nearby',
      method: 'GET',
      data: {
        lat: app.globalData.location.latitude,
        lng: app.globalData.location.longitude,
        mode: that.data.mode,
        distance: 10.0
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
            if (t.aiScore) {
              t.aiScoreText = t.aiScore.toFixed(1);
            }
          });
          that.setData({
            toilets: toilets,
            hasResult: toilets.length > 0,
            loading: false
          });
        } else {
          that.setData({ loading: false, hasResult: false });
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
  }
});
