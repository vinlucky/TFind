var app = getApp();

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  },

  onOldPasswordInput: function (e) {
    this.setData({ oldPassword: e.detail.value });
  },

  onNewPasswordInput: function (e) {
    this.setData({ newPassword: e.detail.value });
  },

  onConfirmPasswordInput: function (e) {
    this.setData({ confirmPassword: e.detail.value });
  },

  submitChange: function () {
    var that = this;
    var oldPassword = this.data.oldPassword;
    var newPassword = this.data.newPassword;
    var confirmPassword = this.data.confirmPassword;

    if (!oldPassword) {
      app.showError('请输入原密码');
      return;
    }
    if (!newPassword) {
      app.showError('请输入新密码');
      return;
    }
    if (newPassword.length < 6) {
      app.showError('新密码长度不能少于6位');
      return;
    }
    if (newPassword !== confirmPassword) {
      app.showError('两次输入的新密码不一致');
      return;
    }

    app.request({
      url: '/api/user/password',
      method: 'PUT',
      data: {
        oldPassword: oldPassword,
        newPassword: newPassword
      },
      success: function (res) {
        if (res.data && res.data.code === 200) {
          app.showSuccess('密码修改成功');
          setTimeout(function () {
            wx.navigateBack();
          }, 1500);
        } else {
          console.error('修改密码失败:', res.data);
          app.showError('修改密码失败');
        }
      },
      fail: function () {
        app.showError('网络错误，请稍后重试');
      }
    });
  }
});
