# EasyBinder

EasyBinder is an alternative field binder for Vaadin 8 that tries to reduce boiler plate code by automating the binding process and relying on javax.validation (JSR 303) for validation.

EasyBinder borrows concepts (and some code) from Vaadin 8, Vaadin 7 and Viritin. EasyBinder is licensed under Apache 2.0.

New features compared with Vaadin 8 standard binder:
- Converter registration support (https://github.com/vaadin/framework/issues/9202).
- JSR 303 bean level validation support (https://github.com/vaadin/framework/issues/8498).
- JSR 303 validation groups support (https://github.com/vaadin/framework/issues/8385).
- JSR 303 @Valid support (https://github.com/vaadin/framework/issues/9520)
- unbind() method.
- Support for automatic binding of nested beans (https://github.com/vaadin/framework/issues/9210)
- Proper null-conversions (https://github.com/vaadin/framework/issues/8441, https://github.com/vaadin/framework/issues/9000 and https://github.com/vaadin/framework/issues/9453)
- Binding with value-provider supports JSR 303 validation (https://github.com/vaadin/framework/issues/8815)
- "Naked objects" inspired automatic form-builder.
- Easily extendable, most internal classes, methods and fields are declared protected.

Limitations compared with Vaadin 8 standard binder:
- No readBean()/writeBean() support, only setBean() is supported.
- No fluent-api binder builder (but can be easily added).

# Usage

EasyBinder has 3 different levels of abstraction:
- BasicBinder is a simple type-safe binder for manually binding properties to fields. 
- ReflectionBinder is an extension to BasicBinder that allows binding of properties to fields based on the property name of the property.
- AutoBinder is an extension to ReflectionBinder that allows automatic POJO property to UI component binding and also automatic UI component generation from POJO properties (aka "Naked objects").  

EasyBinder has the following helper classes:
- ConverterRegistry is a global Converter registry used by ReflectionBinder/AutoBinder to register and lookup converters for automatic property binding.
- ComponentFactoryRegistry is a global Component factory for generating UI Components based on POJO properties ("Naked objects"). 


Given the following Entity:
```
class MyEntity {
	@NotNull
	String name;
	
	int height;
	
	@Temporal(TemporalType.DATE)	
	Date dateOfBirth;
	
	@Temporal(TemporalType.TIMESTAMP)	
	Date timeAndDate;	
	
	... getters and setters...
}
```
It can be bound using one of the following approaches:
## Auto binding to existing Form fields
```
class MyForm {
	TextField name = new TextField("Name");
	TextField height = new TextField("Height");
	DateField dateOfBirth = new DateField("Date of birth");
	DateTimeField timeAndDate = new DateTimeField("Time and date"); 
}

MyForm form = new MyForm();
AutoBinder<MyEntity> binder = new AutoBinder<>(MyEntity.class);
// Perform binding 	
binder.bindInstanceFields(form);
 	
// Add components to form
addComponents(binder.getBoundFields());
 	
// Set entity
MyEntity entity = new MyEntity();
binder.setBean(entity);
```


## Auto creation of Form fields
```
AutoBinder<MyEntity> binder = new AutoBinder<>(MyEntity.class);
// Perform field creation, binding and add components to form
addComponents(
	binder.buildAndBind()
	);
	
// Set entity
MyEntity entity = new MyEntity();
binder.setBean(entity);
```

## Register custom converter
```
ConverterRegistry.getInstance().registerConverter(String.class, Character.class, Converter.from(e -> e.length() == 0 ? Result.ok(null) : (e.length() == 1 ? Result.ok(e.charAt(0)) : Result.error("Must be 1 character")), f -> f == null ? "" : "" + f));	
```
## Register custom field builder
```
ComponentFactoryRegistry().getInstance().addBuildPattern(Date.class, e -> Arrays.asList(e.getAnnotations()).stream().filter(f -> f instanceof Temporal).map(f -> (Temporal)f).filter(f -> f.value() == TemporalType.TIMESTAMP).findAny().isPresent(), e -> new DateTimeField(SharedUtil.camelCaseToHumanFriendly(e.getName())));
```

## Examples

The src/test/java/org/vaadin/easybinder/example folder contains 4 examples of form binding using different approaches (Standard Vaadin BeanBinding, Manual property binding, Automatic property binding and automatic UI component creation and binding).
The examples can be run by running the embedded test server located in src/test/java/org/vaadin/uiserver


# Information for developers

## Help wanted

These are some of the points currently on my TODO list, any help would be appreciated:
- More use cases.
- Code review.
- API doc documentation and supplemental documentation.
- Test code.
- Fluent binder builder like Vaadin 8 standard binder
- More pre-defined default converters and component factories
- Explore and extend the "buildAndBind" ("naked objects") function. Add annotation support.

## Development instructions 

01. Import to your favourite IDE
2. Run main method of the Server class to launch embedded web server that lists all your test UIs at http://localhost:9998
3. Code and test
  * create UI's for various use cases for your add-ons, see examples. These can also work as usage examples for your add-on users.
  * create browser level and integration tests under src/test/java/
  * Browser level tests are executed manually from IDE (JUnit case) or with Maven profile "browsertests" (mvn verify -Pbrowsertests). If you have a setup for solidly working Selenium driver(s), consider enabling that profile by default.
4. Test also in real world projects, on good real integration test is to *create a separate demo project* using vaadin-archetype-application, build a snapshot release ("mvn install") of the add-on and use the snapshot build in it. Note, that you can save this demo project next to your add-on project and save it to same GIT(or some else SCM) repository, just keep them separated for perfect testing.

## Creating releases

1. Push your changes to e.g. Github 
2. Update pom.xml to contain proper SCM coordinates (first time only)
3. Use Maven release plugin (mvn release:prepare; mvn release:perform)
4. Upload the ZIP file generated to target/checkout/target directory to https://vaadin.com/directory service (and/or optionally publish your add-on to Maven central)

