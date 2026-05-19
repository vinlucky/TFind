App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: 'http://localhost:8080',
    location: null,
    recommendMode: 'smart'
  },

  onLaunch: function () {
    var that = this;
    var token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
    }
    var userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.globalData.userInfo = userInfo;
    }
    this.getUserLocation();
    this.detectEnv();
  },

  detectEnv: function () {
    var that = this;
    wx.getSystemInfo({
      success: function (info) {
        if (info.platform !== 'devtools') {
          that.globalData.baseUrl = 'http://172.20.10.3:8080'; //需要改为你的电脑的IP
          console.log('真机环境，使用局域网地址: ' + that.globalData.baseUrl);
          console.log('如无法连接，请在 app.js 的 detectEnv 中修改为电脑的实际局域网IP');
        } else {
          console.log('开发工具环境，使用 localhost');
        }
      }
    });
  },

  request: function (options) {
    var that = this;
    var header = options.header || {};
    if (that.globalData.token) {
      header['Authorization'] = 'Bearer ' + that.globalData.token;
    }
    header['Content-Type'] = header['Content-Type'] || 'application/json';
    wx.request({
      url: that.globalData.baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: function (res) {
        if (res.statusCode === 401) {
          that.globalData.token = null;
          that.globalData.userInfo = null;
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
          setTimeout(function () {
            wx.redirectTo({ url: '/pages/login/login' });
          }, 1500);
          if (options.fail) {
            options.fail(res);
          }
          return;
        }
        if (options.success) {
          options.success(res);
        }
      },
      fail: function (err) {
        console.error('wx.request 请求失败:', {
          url: that.globalData.baseUrl + options.url,
          method: options.method || 'GET',
          errMsg: err.errMsg,
          err: JSON.stringify(err)
        });
        if (options.fail) {
          options.fail(err);
        }
      },
      complete: function () {
        if (options.complete) {
          options.complete();
        }
      }
    });
  },

  getUserLocation: function () {
    var that = this;
    wx.getLocation({
      type: 'gcj02',
      success: function (res) {
        that.globalData.location = {
          latitude: res.latitude,
          longitude: res.longitude
        };
      },
      fail: function () {
        wx.getSetting({
          success: function (res) {
            if (!res.authSetting['scope.userLocation']) {
              wx.showModal({
                title: '位置授权',
                content: '需要获取您的位置信息以提供附近厕所推荐',
                success: function (modalRes) {
                  if (modalRes.confirm) {
                    wx.openSetting();
                  }
                }
              });
            }
          }
        });
      }
    });
  },

  calculateDistance: function (lat1, lng1, lat2, lng2) {
    var Rad = Math.PI / 180;
    var radLat1 = lat1 * Rad;
    var radLat2 = lat2 * Rad;
    var a = radLat1 - radLat2;
    var b = lng1 * Rad - lng2 * Rad;
    var s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
      Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
    s = s * 6378.137;
    s = Math.round(s * 10000) / 10000;
    return s;
  },

  checkLogin: function () {
    if (!this.globalData.token) {
      wx.navigateTo({ url: '/pages/login/login' });
      return false;
    }
    return true;
  },

  showLoading: function (title) {
    wx.showLoading({
      title: title || '加载中...',
      mask: true
    });
  },

  hideLoading: function () {
    wx.hideLoading();
  },

  showError: function (msg) {
    wx.showToast({
      title: msg || '操作失败',
      icon: 'none',
      duration: 2000
    });
  },

  showSuccess: function (msg) {
    wx.showToast({
      title: msg || '操作成功',
      icon: 'success',
      duration: 1500
    });
  }
});
