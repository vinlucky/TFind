var app = getApp();

Page({
  data: {
    userId: '',
    password: '',
    confirmPassword: ''
  },

  onUserIdInput: function (e) {
    this.setData({ userId: e.detail.value });
  },

  onPasswordInput: function (e) {
    this.setData({ password: e.detail.value });
  },

  onConfirmPasswordInput: function (e) {
    this.setData({ confirmPassword: e.detail.value });
  },

  doRegister: function () {
    var that = this;
    if (!that.data.userId) {
      wx.showToast({ title: '请输入用户名', icon: 'none' });
      return;
    }
    var userIdReg = /^[a-zA-Z0-9!@#$%^&*_]+$/;
    if (!userIdReg.test(that.data.userId)) {
      wx.showToast({ title: '用户名只能使用英文字母数字或普通符号', icon: 'none' });
      return;
    }
    if (!that.data.password) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }
    if (that.data.password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' });
      return;
    }
    if (that.data.password !== that.data.confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' });
      return;
    }
    app.showLoading('注册中...');
    app.request({
      url: '/api/user/register',
      method: 'POST',
      data: {
        userId: that.data.userId,
        password: that.data.password
      },
      success: function (res) {
        app.hideLoading();
        if (res.data && res.data.code === 200) {
          app.showSuccess('注册成功');
          setTimeout(function () {
            wx.navigateBack();
          }, 1500);
        } else {
          app.showError(res.data.message || '注册失败');
        }
      },
      fail: function () {
        app.hideLoading();
        app.showError('网络错误');
      }
    });
  },

  goToLogin: function () {
    wx.navigateBack();
  }
});
