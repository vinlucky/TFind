var app = getApp();

Page({
  data: {
    isLogin: false,
    userInfo: null,
    stats: {
      uploadCount: 0,
      reviewCount: 0,
      updateCount: 0
    },
    historyList: [],
    isAdmin: false
  },

  onLoad: function () {},

  onShow: function () {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 });
    }
    this.checkLoginStatus();
  },

  checkLoginStatus: function () {
    var token = app.globalData.token;
    var userInfo = app.globalData.userInfo;
    if (token && userInfo) {
      this.setData({
        isLogin: true,
        userInfo: userInfo,
        isAdmin: userInfo.role === 'ADMIN' || userInfo.role === 'admin'
      });
      this.loadUserStats();
      this.loadHistory();
    } else {
      this.setData({
        isLogin: false,
        userInfo: null,
        isAdmin: false,
        stats: { uploadCount: 0, reviewCount: 0, updateCount: 0 },
        historyList: []
      });
    }
  },

  doLogin: function () {
    wx.navigateTo({ url: '/pages/login/login' });
  },

  loadUserStats: function () {
    var that = this;
    app.request({
      url: '/api/profile/stats',
      method: 'GET',
      success: function (res) {
        if (res.data && res.data.code === 200) {
          that.setData({ stats: res.data.data });
        }
      }
    });
  },

  loadHistory: function () {
    var that = this;
    if (!app.globalData.userInfo) return;
    app.request({
      url: '/api/toilet/list',
      method: 'GET',
      success: function (res) {
        if (res.data && res.data.code === 200) {
          var allToilets = res.data.data || [];
          var userId = app.globalData.userInfo.userId;
          var myToilets = allToilets.filter(function (t) {
            return t.openid === userId;
          });
          that.setData({ historyList: myToilets });
        }
      }
    });
  },

  goToDetail: function (e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/toilet-detail/toilet-detail?id=' + id });
  },

  goToAdmin: function () {
    wx.navigateTo({ url: '/pages/admin/admin' });
  },

  goToChangePassword: function () {
    wx.navigateTo({ url: '/pages/change-password/change-password' });
  },

  logout: function () {
    var that = this;
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: function (res) {
        if (res.confirm) {
          app.globalData.token = null;
          app.globalData.userInfo = null;
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          that.setData({
            isLogin: false,
            userInfo: null,
            isAdmin: false,
            stats: { uploadCount: 0, reviewCount: 0, updateCount: 0 },
            historyList: []
          });
          app.showSuccess('已退出登录');
        }
      }
    });
  }
});
