var app = getApp();

Page({
  data: {
    userId: '',
    password: ''
  },

  onUserIdInput: function (e) {
    this.setData({ userId: e.detail.value });
  },

  onPasswordInput: function (e) {
    this.setData({ password: e.detail.value });
  },

  doLogin: function () {
    var that = this;
    if (!that.data.userId) {
      wx.showToast({ title: '请输入用户名', icon: 'none' });
      return;
    }
    if (!that.data.password) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }
    app.showLoading('登录中...');
    app.request({
      url: '/api/user/login',
      method: 'POST',
      data: {
        userId: that.data.userId,
        password: that.data.password
      },
      success: function (res) {
        app.hideLoading();
        if (res.data && res.data.code === 200) {
          var token = res.data.data;
          app.globalData.token = token;
          wx.setStorageSync('token', token);
          app.request({
            url: '/api/user/info',
            method: 'GET',
            success: function (infoRes) {
              if (infoRes.data && infoRes.data.code === 200) {
                var userInfo = infoRes.data.data;
                app.globalData.userInfo = userInfo;
                wx.setStorageSync('userInfo', userInfo);
                app.showSuccess('登录成功');
                setTimeout(function () {
                  wx.switchTab({ url: '/pages/index/index' });
                }, 1000);
              }
            }
          });
        } else {
          app.showError(res.data.message || '登录失败');
        }
      },
      fail: function (err) {
        app.hideLoading();
        app.showError('网络错误: ' + (err.errMsg || '未知错误'));
      }
    });
  },

  goToRegister: function () {
    wx.navigateTo({ url: '/pages/register/register' });
  }
});
