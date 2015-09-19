# ec2-util
ec2-util reduces steps to use AWS EC2, than AWS Console.
- You don't need to know Ec2 Instance ID. You can start/stop Ec2 Instance, specified Ec2 Instance Name with ```-n``` option.
- You don't need to allocate / associate / disassociate / release EIP to click Console.

ec2-util provides following features.
- Start EC2 Instance, allocate EIP, and associate EIP to EC2 Instance, finally , to create DNS recorde SubDomain ```-d``` option.
- Stop EC2 Instance, disassociate EIP from EC2 Instance, and release EIP.


## Usage

### Start Ec2 Instance
Execute following command.

```
bin/ec2-util.sh -n <Your Ec2 Instance's Name> start
```

If exists Ec2 Instance with specified Name, output this.

```shell
com.betahikaru.aws.command.Ec2StartCommand
Exists instance (id = i-7eddbb8d)
Starting Instance (i-7eddbb8c: stopped=>pending)
Allocated Address(54.64.40.43, eipalloc-71a87f15)
Associated Address(54.64.40.43, eipassoc-ff90b09b)
Hosted Zone Id: Z1F2R7YCCWCCCC
Attach domain : dev.betahikaru.com
```

### Stop Ec2 Instance
Execute following command.

```shell
bin/ec2-util.sh -n <Your Ec2 Instance Name> stop
```

If exists instance with specified Name, output this.

```shell
com.betahikaru.aws.command.Ec2StopCommand
Exists instance (id = i-7eddbb8d)
Stopping Instance (i-7eddbb8c: running=>stopping)
Disassociated Address (52.69.199.169)
Released Address (52.69.199.169)
```

If not exists instance with specified Name, output this.

```shell
com.betahikaru.aws.command.Ec2StopCommand
Not exists instance (name = Wagahai).
```

## Require
* AWS Account
* JRE/JDK 1.7 or later

## Setup

Download this repository, and build this project.

```
git clone git@github.com:betahikaru/ec2-util.git
cd ec2-util
mvn clean compile jar:jar package
```

Set Access Key and Secret Key for IAM Account to conf/credentials.properties.

```
aws.region = <Target region : ex) AP_NORTHEAST_1>
aws.access_key_id=<Your access key>
aws.secret_access_key=<Your secret access key>
```

## Licence
MIT

## Author
betahikaru (@betahikaru)
