//
//  LoginViewController.m
//  BaiduASRDemo
//
//  Created by Wu,Yunpeng(AI2B) on 2019/5/9.
//  Copyright © 2019 Cloud. All rights reserved.
//

#import "LoginViewController.h"
#import "ASRConfig.h"

@interface LoginViewController ()

@property (nonatomic, weak) IBOutlet UITextField *userNameTF;
@property (nonatomic, weak) IBOutlet UITextField *passWordTF;

@property (nonatomic, weak) IBOutlet UIButton *loginBtn;

@end

@implementation LoginViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    ASRConfig *config = [ASRConfig config];
    NSString *userName = config.userName;
    NSString *passWord = config.passWord;
    self.userNameTF.text = userName;
    self.passWordTF.text = passWord;
    
    NSString *buttonTitle = @"Login";
    if (userName.length > 0 && passWord.length > 0) {
        buttonTitle = @"Logout";
    }
    [self.loginBtn setTitle:buttonTitle forState:UIControlStateNormal];
}

- (IBAction)back:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)login:(id)sender {
    [self.userNameTF resignFirstResponder];
    [self.passWordTF resignFirstResponder];
    
    if ([self.loginBtn.titleLabel.text isEqualToString:@"Login"]) {
        [self loginActon];
    }else {
        [self logoutAction];
    }
}

- (void)loginActon {
    NSString *userName = self.userNameTF.text;
    NSString *passWord = self.passWordTF.text;
    
    if (userName.length > 0 && passWord.length > 0) {
        ASRConfig *config = [ASRConfig config];
        config.userName = userName;
        config.passWord = passWord;
        
        [config login];
        
        [self.loginBtn setTitle:@"Logout" forState:UIControlStateNormal];
        
        [self performSelector:@selector(back:) withObject:nil afterDelay:1.0];
    }else {
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil message:@"请填写正确的用户名密码" preferredStyle:UIAlertControllerStyleAlert];
        [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:nil]];
        [self presentViewController:alert animated:YES completion:nil];
    }    
}

- (void)logoutAction {
    self.userNameTF.text = @"";
    self.passWordTF.text = @"";
    
    ASRConfig *config = [ASRConfig config];
    config.userName = @"";
    config.passWord = @"";
    
    [config logout];
    
    [self.loginBtn setTitle:@"Login" forState:UIControlStateNormal];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
